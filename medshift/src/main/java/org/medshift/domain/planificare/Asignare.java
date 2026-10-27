package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entitate in agregatul GraficLunar: un angajat pus pe o tura.
 * Metodele care schimba starea sunt package-private: doar GraficLunar le poate apela.
 */
public class Asignare implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "Asignarea trebuie sa indice angajatul.")
	private Integer idAngajat;          // referinta prin identitate catre agregatul Angajat
	@NotNull(message = "Gradul medical este obligatoriu.")
	private GradMedical grad;
	@NotNull
	private StareAsignare stare = StareAsignare.PROPUSA;
	private LocalDateTime termenConfirmare;
	@Size(max = 255, message = "Motivul contestatiei are maxim 255 de caractere.")
	private String motivContestare;

	public Asignare() {
	}

	Asignare(Integer idAngajat, GradMedical grad) {
		this.idAngajat = Objects.requireNonNull(idAngajat, "idAngajat");
		this.grad = Objects.requireNonNull(grad, "grad");
	}

	/* ---- tranzitii de stare (apelate doar din GraficLunar) ---- */

	/** La publicare: porneste fereastra de confirmare (C3: dataPublicare + 3 zile). */
	void deschideConfirmarea(LocalDateTime dataPublicare) {
		this.stare = StareAsignare.IN_ASTEPTARE_CONFIRMARE;
		this.termenConfirmare = dataPublicare.plusDays(3);
	}

	void confirma() {
		if (stare != StareAsignare.IN_ASTEPTARE_CONFIRMARE && stare != StareAsignare.CONTESTATA)
			throw new DomainException("Asignarea nu poate fi confirmata din starea " + stare);
		stare = StareAsignare.CONFIRMATA;
	}

	void contesta(String motiv, LocalDateTime acum) {
		if (stare != StareAsignare.IN_ASTEPTARE_CONFIRMARE)
			throw new DomainException("Doar o asignare in asteptare poate fi contestata.");
		if (acum.isAfter(termenConfirmare))
			throw new DomainException("Fereastra de confirmare de 3 zile a expirat.");
		if (motiv == null || motiv.isBlank())
			throw new DomainException("Contestatia trebuie sa aiba un motiv.");
		this.motivContestare = motiv;
		this.stare = StareAsignare.CONTESTATA;
	}

	/** WF5: neconfirmarea pana la termen = confirmare tacita. */
	boolean confirmaTacitDaca(LocalDateTime acum) {
		if (stare == StareAsignare.IN_ASTEPTARE_CONFIRMARE && acum.isAfter(termenConfirmare)) {
			stare = StareAsignare.CONFIRMATA;
			return true;
		}
		return false;
	}

	void anuleaza() {
		stare = StareAsignare.ANULATA;
	}

	public boolean esteActiva() {
		return stare.esteActiva();
	}

	/* ---- getteri ---- */
	public Integer getIdAngajat() { return idAngajat; }
	public GradMedical getGrad() { return grad; }
	public StareAsignare getStare() { return stare; }
	public LocalDateTime getTermenConfirmare() { return termenConfirmare; }
	public String getMotivContestare() { return motivContestare; }

	@Override
	public String toString() {
		return "\n\t\tAsignare [idAngajat=" + idAngajat + ", grad=" + grad + ", stare=" + stare + "]";
	}
}
