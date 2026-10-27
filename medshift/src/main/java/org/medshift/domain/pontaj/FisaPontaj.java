package org.medshift.domain.pontaj;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.IntervalOrar;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/** Aggregate Root (Pontaj): fisa lunara de pontaj a unui angajat (CF3.1 - CF3.4). */
public class FisaPontaj implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer idFisa;
	@NotNull(message = "Fisa trebuie sa apartina unui angajat.")
	private Integer idAngajat;
	@NotNull(message = "Fisa trebuie sa apartina unei sectii.")
	private Integer idSectie;                 // sectia graficului din care provin turele
	@NotNull(message = "Luna fisei este obligatorie.")
	private LunaCalendaristica luna;
	@NotNull
	private StareFisa stare = StareFisa.GENERATA;
	@Valid
	private List<LiniePontaj> linii = new ArrayList<>();
	@Valid
	private List<Neconformitate> neconformitati = new ArrayList<>();

	public FisaPontaj() {
	}

	public FisaPontaj(Integer idFisa, Integer idAngajat, Integer idSectie, LunaCalendaristica luna) {
		this.idFisa = idFisa;
		this.idAngajat = Objects.requireNonNull(idAngajat, "idAngajat");
		this.idSectie = Objects.requireNonNull(idSectie, "idSectie");
		this.luna = Objects.requireNonNull(luna, "luna");
	}

	/** Adauga o tura lucrata. Doar cat fisa e GENERATA sau NECONFORMA (in corectare). */
	public LiniePontaj adaugaLinie(Integer idTura, LocalDate data, TipTura tipTura, IntervalOrar interval,
			List<SegmentOre> segmente) {
		cereStare(StareFisa.GENERATA, StareFisa.NECONFORMA);
		if (!luna.contine(data))
			throw new DomainException("Tura din " + data + " nu apartine lunii " + luna);
		if (linii.stream().anyMatch(l -> l.getInterval().seSuprapuneCu(interval)))
			throw new DomainException("Tura din " + data + " se suprapune cu o alta tura din fisa.");
		if (linii.stream().anyMatch(l -> l.getIdTura().equals(idTura)))
			throw new DomainException("Tura " + idTura + " este deja in fisa.");
		LiniePontaj linie = new LiniePontaj(idTura, data, tipTura, interval, segmente);
		linii.add(linie);
		return linie;
	}

	public void eliminaLinie(Integer idTura) {
		cereStare(StareFisa.GENERATA, StareFisa.NECONFORMA);
		linii.removeIf(l -> l.getIdTura().equals(idTura));
	}

	/** Rezultatul verificarii legale (CF3.3): lista goala = fisa valida. */
	public void inregistreazaVerificare(List<Neconformitate> gasite) {
		cereStare(StareFisa.GENERATA, StareFisa.NECONFORMA);
		neconformitati = new ArrayList<>(gasite);
		stare = gasite.isEmpty() ? StareFisa.VALIDATA_LEGAL : StareFisa.NECONFORMA;
	}

	/** CF3.4: HR aproba doar o fisa validata legal (regula A10). */
	public void aprobaHR() {
		cereStare(StareFisa.VALIDATA_LEGAL);
		stare = StareFisa.APROBATA_HR;
	}

	/** HR respinge fisa pentru corectii; ea revine in lucru. */
	public void respingeHR(String motiv) {
		cereStare(StareFisa.VALIDATA_LEGAL);
		neconformitati.add(new Neconformitate("HR", motiv, null));
		stare = StareFisa.NECONFORMA;
	}

	void marcheazaExportata() {           // apelata doar de BorderouSalarial (acelasi pachet)
		cereStare(StareFisa.APROBATA_HR);
		stare = StareFisa.EXPORTATA;
	}

	/** Regula C5: totalurile sunt suma liniilor. */
	public TotalOre totaluri() {
		return linii.stream().map(LiniePontaj::totaluri).reduce(TotalOre.ZERO, TotalOre::plus);
	}

	private void cereStare(StareFisa... permise) {
		for (StareFisa s : permise)
			if (stare == s)
				return;
		throw new DomainException("Operatia nu este permisa pentru o fisa in starea " + stare);
	}

	/* ---- getteri ---- */
	public Integer getIdFisa() { return idFisa; }
	public Integer getIdAngajat() { return idAngajat; }
	public Integer getIdSectie() { return idSectie; }
	public LunaCalendaristica getLuna() { return luna; }
	public StareFisa getStare() { return stare; }
	public List<LiniePontaj> getLinii() { return Collections.unmodifiableList(linii); }
	public List<Neconformitate> getNeconformitati() { return Collections.unmodifiableList(neconformitati); }

	public void setIdFisa(Integer idFisa) {
		if (this.idFisa != null)
			throw new DomainException("ID-ul nu se mai poate schimba.");
		this.idFisa = idFisa;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof FisaPontaj f && idFisa != null && idFisa.equals(f.idFisa);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idFisa);
	}

	@Override
	public String toString() {
		return "\nFisaPontaj [id=" + idFisa + ", angajat=" + idAngajat + ", luna=" + luna + ", " + stare
				+ ", linii=" + linii.size() + ", total=" + totaluri().total() + " h]";
	}
}
