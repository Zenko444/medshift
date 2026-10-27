package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Entitate in agregatul GraficLunar: o tura dintr-o zi (zi / noapte / garda). */
public class Tura implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull
	private Integer idTura;
	@NotNull(message = "Tura trebuie sa aiba o data.")
	private LocalDate data;
	@NotNull(message = "Tipul turei este obligatoriu.")
	private TipTura tip;
	@NotNull
	private IntervalOrar interval;
	@NotNull
	private StareTura stare = StareTura.PLANIFICATA;
	@Valid
	private List<Asignare> asignari = new ArrayList<>();

	public Tura() {
	}

	Tura(Integer idTura, LocalDate data, TipTura tip) {
		this.idTura = Objects.requireNonNull(idTura, "idTura");
		this.data = Objects.requireNonNull(data, "data");
		this.tip = Objects.requireNonNull(tip, "tip");
		this.interval = tip.intervalPentru(data);
	}

	/* ---- interogari ---- */

	public List<Asignare> asignariActive() {
		return asignari.stream().filter(Asignare::esteActiva).toList();
	}

	public long numarAsignati(GradMedical grad) {
		return asignariActive().stream().filter(a -> a.getGrad() == grad).count();
	}

	public boolean areAngajat(Integer idAngajat) {
		return gasesteAsignareActiva(idAngajat).isPresent();
	}

	Optional<Asignare> gasesteAsignareActiva(Integer idAngajat) {
		return asignariActive().stream().filter(a -> a.getIdAngajat().equals(idAngajat)).findFirst();
	}

	/** C2: asignati / necesar total; 1.0 = acoperita complet. */
	public double gradAcoperire(MatriceAcoperire matrice) {
		int necesar = matrice.necesarTotal(tip);
		if (necesar == 0)
			return 1.0;
		long acoperit = matrice.cerintePentru(tip).stream()
				.mapToLong(c -> Math.min(numarAsignati(c.grad()), c.numarMinim()))
				.sum();
		return (double) acoperit / necesar;
	}

	public boolean esteAcoperita(MatriceAcoperire matrice) {
		return matrice.cerintePentru(tip).stream().allMatch(c -> numarAsignati(c.grad()) >= c.numarMinim());
	}

	/* ---- modificari (doar din GraficLunar) ---- */

	void adaugaAsignare(Asignare asignare) {
		asignari.add(asignare);
	}

	void actualizeazaStareaDupaAcoperire(MatriceAcoperire matrice) {
		if (stare == StareTura.EFECTUATA)
			return;
		stare = esteAcoperita(matrice) ? StareTura.PLANIFICATA : StareTura.DESCOPERITA;
	}

	void marcheazaEfectuata() {
		stare = StareTura.EFECTUATA;
	}

	/* ---- getteri ---- */
	public Integer getIdTura() { return idTura; }
	public LocalDate getData() { return data; }
	public TipTura getTip() { return tip; }
	public IntervalOrar getInterval() { return interval; }
	public StareTura getStare() { return stare; }
	public List<Asignare> getAsignari() { return Collections.unmodifiableList(asignari); }

	@Override
	public boolean equals(Object o) {
		return o instanceof Tura t && Objects.equals(idTura, t.idTura);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idTura);
	}

	@Override
	public String toString() {
		return "\n\tTura [idTura=" + idTura + ", data=" + data + ", tip=" + tip + ", stare=" + stare
				+ ", asignari=" + asignariActive() + "]";
	}
}
