package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root (Core Domain: Planificarea si Optimizarea Turelor).
 * Toate modificarile pe Tura si Asignare trec prin aceasta clasa, ca invariantii A1-A8 sa fie garantati.
 */
public class GraficLunar implements Serializable, Comparable<GraficLunar> {
	private static final long serialVersionUID = 1L;

	private Integer idGrafic;
	@NotNull(message = "Graficul trebuie sa apartina unei sectii.")
	private Integer idSectie;                 // referinta prin identitate
	@NotNull(message = "Luna graficului este obligatorie.")
	private LunaCalendaristica luna;
	@NotNull
	private StareGrafic stare = StareGrafic.IN_COLECTARE_DOLEANTE;
	@NotNull(message = "Termenul de doleante este obligatoriu.")
	private LocalDate termenDoleante;
	@FutureOrPresent(message = "Data publicarii nu poate fi in trecut.")
	private LocalDateTime dataPublicare;
	@NotNull @Valid
	private MatriceAcoperire matrice = MatriceAcoperire.goala();
	@Valid
	private List<Tura> ture = new ArrayList<>();

	public GraficLunar() {
	}

	/** CF1.1: initializarea graficului pentru o luna. */
	public GraficLunar(Integer idGrafic, Integer idSectie, LunaCalendaristica luna, LocalDate termenDoleante) {
		this.idGrafic = Objects.requireNonNull(idGrafic, "idGrafic");
		this.idSectie = Objects.requireNonNull(idSectie, "idSectie");
		this.luna = Objects.requireNonNull(luna, "luna");
		this.termenDoleante = Objects.requireNonNull(termenDoleante, "termenDoleante");
		if (!termenDoleante.isBefore(luna.zile().get(0)))
			throw new DomainException("Termenul de doleante trebuie sa fie inainte de inceputul lunii.");
	}

	/* =================== CF1.2 / structura =================== */

	public void configureazaNecesar(MatriceAcoperire matriceNoua) {
		cereStare(StareGrafic.IN_COLECTARE_DOLEANTE, StareGrafic.IN_AJUSTARE);
		this.matrice = Objects.requireNonNull(matriceNoua);
	}

	public Tura adaugaTura(Integer idTura, LocalDate data, TipTura tip) {
		cereStare(StareGrafic.IN_COLECTARE_DOLEANTE, StareGrafic.IN_AJUSTARE);
		if (!luna.contine(data))
			throw new DomainException("Data " + data + " nu apartine lunii " + luna);
		boolean exista = ture.stream().anyMatch(t -> t.getData().equals(data) && t.getTip() == tip);
		if (exista)
			throw new DomainException("Exista deja o tura " + tip + " pe " + data);
		Tura tura = new Tura(idTura, data, tip);
		ture.add(tura);
		return tura;
	}

	/* =================== CF1.3 / generare + ajustare =================== */

	/** WF2: trecerea in ajustare, dupa termenul de doleante si cu matricea configurata. */
	public void incepeAjustarea(LocalDate azi) {
		cereStare(StareGrafic.IN_COLECTARE_DOLEANTE);
		if (!azi.isAfter(termenDoleante))
			throw new DomainException("Termenul de doleante (" + termenDoleante + ") nu a expirat.");
		if (!matrice.esteConfigurata())
			throw new DomainException("Matricea de acoperire nu este configurata.");
		if (ture.isEmpty())
			throw new DomainException("Graficul nu are ture.");
		stare = StareGrafic.IN_AJUSTARE;
	}

	/** Folosita de strategia de generare si de ajustarile manuale (A2, A3, A7). */
	public void asigneaza(Integer idTura, Integer idAngajat, GradMedical grad) {
		cereStare(StareGrafic.IN_AJUSTARE);
		adaugaAsignareVerificata(gasesteTura(idTura), idAngajat, grad);
	}

	public void eliminaAsignare(Integer idTura, Integer idAngajat) {
		cereStare(StareGrafic.IN_AJUSTARE);
		asignareActiva(gasesteTura(idTura), idAngajat).anuleaza();
	}

	/** Verifica, fara sa modifice nimic, daca angajatul poate fi pus pe tura (A2, A3). Folosita de strategiile de generare. */
	public boolean poateAsigna(Integer idTura, Integer idAngajat) {
		Tura tura = gasesteTura(idTura);
		if (tura.areAngajat(idAngajat))
			return false;
		List<Tura> program = new ArrayList<>(tureAngajat(idAngajat));
		program.add(tura);
		return incalcariProgram(idAngajat, program).isEmpty();
	}

	/** Lista incalcarilor restrictiilor dure (A1-A3); goala = graficul poate fi publicat. */
	public List<String> verificaRestrictii() {
		List<String> incalcari = new ArrayList<>();
		for (Tura t : ture)
			for (CerintaAcoperire c : matrice.cerintePentru(t.getTip()))
				if (t.numarAsignati(c.grad()) < c.numarMinim())
					incalcari.add("A1: tura " + t.getTip() + " din " + t.getData() + " are "
							+ t.numarAsignati(c.grad()) + "/" + c.numarMinim() + " " + c.grad());
		ture.stream().flatMap(t -> t.asignariActive().stream()).map(Asignare::getIdAngajat).distinct()
				.forEach(id -> incalcari.addAll(incalcariProgram(id, tureAngajat(id))));
		return incalcari;
	}

	/* =================== CF1.4 / publicare =================== */

	public void publica(LocalDateTime acum) {
		cereStare(StareGrafic.IN_AJUSTARE);
		List<String> incalcari = verificaRestrictii();
		if (!incalcari.isEmpty())
			throw new DomainException("Graficul nu poate fi publicat: " + incalcari);
		stare = StareGrafic.PUBLICAT;
		dataPublicare = acum;
		ture.forEach(t -> t.asignariActive().forEach(a -> a.deschideConfirmarea(acum)));
	}

	/* =================== CF1.5 / confirmare =================== */

	public void confirmaAsignare(Integer idTura, Integer idAngajat) {
		cereStare(StareGrafic.PUBLICAT);
		asignareActiva(gasesteTura(idTura), idAngajat).confirma();
	}

	public void contestaAsignare(Integer idTura, Integer idAngajat, String motiv, LocalDateTime acum) {
		cereStare(StareGrafic.PUBLICAT);
		asignareActiva(gasesteTura(idTura), idAngajat).contesta(motiv, acum);
	}

	/** Seful de sectie accepta contestatia: asignarea se anuleaza, tura poate deveni Descoperita (WF6). */
	public void accepteContestatia(Integer idTura, Integer idAngajat) {
		cereStare(StareGrafic.PUBLICAT);
		Tura tura = gasesteTura(idTura);
		Asignare a = asignareActiva(tura, idAngajat);
		if (a.getStare() != StareAsignare.CONTESTATA)
			throw new DomainException("Asignarea nu este contestata.");
		a.anuleaza();
		tura.actualizeazaStareaDupaAcoperire(matrice);
	}

	/** Rulata de timer: WF5, confirmare tacita dupa 3 zile. Returneaza cate asignari s-au confirmat. */
	public int aplicaConfirmariTacite(LocalDateTime acum) {
		cereStare(StareGrafic.PUBLICAT);
		int n = 0;
		for (Tura t : ture)
			for (Asignare a : t.asignariActive())
				if (a.confirmaTacitDaca(acum))
					n++;
		return n;
	}

	/* =================== CF2.4 / concedii + realocare =================== */

	/** Concediu aprobat: asignarile angajatului din perioada se anuleaza (A6, R2). Returneaza turele afectate. */
	public List<Tura> marcheazaDescoperita(Integer idAngajat, LocalDate deLa, LocalDate panaLa) {
		cereStare(StareGrafic.PUBLICAT);
		List<Tura> afectate = new ArrayList<>();
		for (Tura t : ture) {
			if (t.getData().isBefore(deLa) || t.getData().isAfter(panaLa))
				continue;
			t.gasesteAsignareActiva(idAngajat).ifPresent(a -> {
				a.anuleaza();
				t.actualizeazaStareaDupaAcoperire(matrice);
				afectate.add(t);
			});
		}
		return afectate;
	}

	public void realoca(Integer idTura, Integer idAngajat, GradMedical grad, LocalDateTime acum) {
		cereStare(StareGrafic.PUBLICAT);
		Tura tura = gasesteTura(idTura);
		Asignare noua = adaugaAsignareVerificata(tura, idAngajat, grad);
		noua.deschideConfirmarea(acum);
		tura.actualizeazaStareaDupaAcoperire(matrice);
	}

	/* =================== CF2.3 / schimb Peer-to-Peer =================== */

	/**
	 * Verifica "pe ciorna", fara sa modifice nimic, daca schimbul de tura ar respecta regulile (A8).
	 * Lista goala = schimbul e posibil. Folosita si de contextul Personal la cererea de schimb (UC2.3).
	 */
	public List<String> verificaSchimb(Integer idTuraA, Integer idAngajatA, Integer idTuraB, Integer idAngajatB,
			LocalDateTime acum) {
		List<String> incalcari = new ArrayList<>();
		if (stare != StareGrafic.PUBLICAT)
			return List.of("Schimburile sunt permise doar pe un grafic publicat (stare actuala: " + stare + ").");
		Tura turaA = gasesteTura(idTuraA);
		Tura turaB = gasesteTura(idTuraB);
		var a = turaA.gasesteAsignareActiva(idAngajatA);
		var b = turaB.gasesteAsignareActiva(idAngajatB);
		if (a.isEmpty() || b.isEmpty())
			return List.of("Fiecare angajat trebuie sa fie asignat pe tura pe care o ofera.");
		if (!turaA.getInterval().inceput().isAfter(acum) || !turaB.getInterval().inceput().isAfter(acum))
			return List.of("Se pot schimba doar ture viitoare.");
		if (turaA.areAngajat(idAngajatB) || turaB.areAngajat(idAngajatA))
			return List.of("Unul dintre angajati este deja pe tura celuilalt.");

		// programul ipotetic al fiecaruia dupa schimb
		List<Tura> programA = new ArrayList<>(tureAngajat(idAngajatA));
		programA.remove(turaA);
		programA.add(turaB);
		List<Tura> programB = new ArrayList<>(tureAngajat(idAngajatB));
		programB.remove(turaB);
		programB.add(turaA);
		incalcari.addAll(incalcariProgram(idAngajatA, programA));
		incalcari.addAll(incalcariProgram(idAngajatB, programB));
		GradMedical gradA = a.get().getGrad(), gradB = b.get().getGrad();
		if (gradA != gradB) { // A1 cand gradele difera
			if (turaA.numarAsignati(gradA) - 1 < matrice.necesarPentru(turaA.getTip(), gradA))
				incalcari.add("A1: tura " + idTuraA + " ar ramane fara " + gradA);
			if (turaB.numarAsignati(gradB) - 1 < matrice.necesarPentru(turaB.getTip(), gradB))
				incalcari.add("A1: tura " + idTuraB + " ar ramane fara " + gradB);
		}
		return incalcari;
	}

	/** Inverseaza atomic doua asignari (A8): totul sau nimic. */
	public void schimbaAsignari(Integer idTuraA, Integer idAngajatA, Integer idTuraB, Integer idAngajatB,
			LocalDateTime acum) {
		cereStare(StareGrafic.PUBLICAT);
		// 1. verificam pe ciorna, INAINTE de a modifica ceva
		List<String> incalcari = verificaSchimb(idTuraA, idAngajatA, idTuraB, idAngajatB, acum);
		if (!incalcari.isEmpty())
			throw new DomainException("Schimbul ar incalca restrictiile: " + incalcari);

		// 2. abia acum modificam
		Tura turaA = gasesteTura(idTuraA);
		Tura turaB = gasesteTura(idTuraB);
		Asignare a = asignareActiva(turaA, idAngajatA);
		Asignare b = asignareActiva(turaB, idAngajatB);
		a.anuleaza();
		b.anuleaza();
		Asignare nouaA = new Asignare(idAngajatB, b.getGrad());
		Asignare nouaB = new Asignare(idAngajatA, a.getGrad());
		nouaA.deschideConfirmarea(acum);
		nouaA.confirma();
		nouaB.deschideConfirmarea(acum);
		nouaB.confirma();
		turaA.adaugaAsignare(nouaA);
		turaB.adaugaAsignare(nouaB);
	}

	/* =================== CF1.6 / finalizare =================== */

	public void finalizeaza(LocalDate azi) {
		cereStare(StareGrafic.PUBLICAT);
		if (!azi.isAfter(luna.ultimaZi()))
			throw new DomainException("Luna " + luna + " nu s-a incheiat.");
		ture.forEach(Tura::marcheazaEfectuata);
		stare = StareGrafic.FINALIZAT;
	}

	/* =================== interogari (C2, view-uri) =================== */

	public List<Tura> tureAngajat(Integer idAngajat) {
		return ture.stream().filter(t -> t.areAngajat(idAngajat)).toList();
	}

	public List<Tura> tureDescoperite() {
		return ture.stream().filter(t -> !t.esteAcoperita(matrice)).toList();
	}

	public long numarGarzi(Integer idAngajat) {
		return tureAngajat(idAngajat).stream().filter(t -> t.getTip() == TipTura.GARDA).count();
	}

	public Tura gasesteTura(Integer idTura) {
		return ture.stream().filter(t -> t.getIdTura().equals(idTura)).findFirst()
				.orElseThrow(() -> new DomainException("Tura " + idTura + " nu exista in grafic."));
	}

	/* =================== reguli interne =================== */

	private Asignare adaugaAsignareVerificata(Tura tura, Integer idAngajat, GradMedical grad) {
		if (tura.areAngajat(idAngajat))
			throw new DomainException("Angajatul " + idAngajat + " este deja pe tura " + tura.getIdTura());
		List<Tura> program = new ArrayList<>(tureAngajat(idAngajat));
		program.add(tura);
		List<String> incalcari = incalcariProgram(idAngajat, program);
		if (!incalcari.isEmpty())
			throw new DomainException(String.join("; ", incalcari));
		Asignare a = new Asignare(idAngajat, grad);
		tura.adaugaAsignare(a);
		return a;
	}

	/** A2 (fara suprapuneri) si A3 (pauza minima dupa fiecare tura) pentru programul unui angajat. */
	private List<String> incalcariProgram(Integer idAngajat, List<Tura> program) {
		List<String> incalcari = new ArrayList<>();
		List<Tura> sortat = program.stream()
				.sorted(Comparator.comparing(t -> t.getInterval().inceput())).toList();
		for (int i = 1; i < sortat.size(); i++) {
			Tura prev = sortat.get(i - 1);
			Tura cur = sortat.get(i);
			double pauza = prev.getInterval().pauzaPanaLa(cur.getInterval());
			if (pauza < 0)
				incalcari.add("A2: angajatul " + idAngajat + " are ture suprapuse pe " + prev.getData()
						+ " si " + cur.getData());
			else if (pauza < prev.getTip().pauzaMinimaOre())
				incalcari.add("A3: angajatul " + idAngajat + " are doar " + pauza + " h pauza dupa "
						+ prev.getTip() + " din " + prev.getData() + " (minim " + prev.getTip().pauzaMinimaOre() + " h)");
		}
		return incalcari;
	}

	private Asignare asignareActiva(Tura tura, Integer idAngajat) {
		return tura.gasesteAsignareActiva(idAngajat).orElseThrow(() -> new DomainException(
				"Angajatul " + idAngajat + " nu este asignat pe tura " + tura.getIdTura()));
	}

	private void cereStare(StareGrafic... permise) {
		for (StareGrafic s : permise)
			if (stare == s)
				return;
		throw new DomainException("Operatia nu este permisa in starea " + stare);
	}

	/* =================== getteri, equals, toString =================== */
	public Integer getIdGrafic() { return idGrafic; }
	public Integer getIdSectie() { return idSectie; }
	public LunaCalendaristica getLuna() { return luna; }
	public StareGrafic getStare() { return stare; }
	public LocalDate getTermenDoleante() { return termenDoleante; }
	public LocalDateTime getDataPublicare() { return dataPublicare; }
	public MatriceAcoperire getMatrice() { return matrice; }
	public List<Tura> getTure() { return Collections.unmodifiableList(ture); }

	/** Folosit de repository cand graficul nu are inca ID (ca in ProjectEntityRepositoryImpl). */
	public void setIdGrafic(Integer idGrafic) {
		if (this.idGrafic != null)
			throw new DomainException("ID-ul graficului nu se mai poate schimba.");
		this.idGrafic = idGrafic;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof GraficLunar g && Objects.equals(idGrafic, g.idGrafic);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idGrafic);
	}

	@Override
	public int compareTo(GraficLunar alt) {
		return this.idGrafic.compareTo(alt.idGrafic);
	}

	@Override
	public String toString() {
		return "\nGraficLunar [idGrafic=" + idGrafic + ", idSectie=" + idSectie + ", luna=" + luna
				+ ", stare=" + stare + ", ture=" + ture.size() + "]";
	}
}
