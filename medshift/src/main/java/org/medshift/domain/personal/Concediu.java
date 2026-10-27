package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.Perioada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/** Aggregate Root (Personal): o cerere de concediu (CF2.4). Urgenta medicala e aprobata automat. */
public class Concediu implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer idConcediu;
	@NotNull(message = "Concediul trebuie sa apartina unui angajat.")
	private Integer idAngajat;
	@NotNull(message = "Tipul concediului este obligatoriu.")
	private TipConcediu tip;
	@NotNull(message = "Perioada concediului este obligatorie.") @Valid
	private Perioada perioada;
	@NotNull
	private StareConcediu stare = StareConcediu.SOLICITAT;
	@Size(max = 255)
	private String motivRespingere;

	public Concediu() {
	}

	public Concediu(Integer idConcediu, Integer idAngajat, TipConcediu tip, Perioada perioada) {
		this.idConcediu = idConcediu;
		this.idAngajat = Objects.requireNonNull(idAngajat, "idAngajat");
		this.tip = Objects.requireNonNull(tip, "tip");
		this.perioada = Objects.requireNonNull(perioada, "perioada");
		if (tip == TipConcediu.URGENTA_MEDICALA)
			this.stare = StareConcediu.APROBAT;          // R2: urgenta marcheaza imediat turele descoperite
	}

	public void aproba() {
		if (stare != StareConcediu.SOLICITAT)
			throw new DomainException("Doar un concediu solicitat poate fi aprobat (stare actuala: " + stare + ").");
		stare = StareConcediu.APROBAT;
	}

	public void respinge(String motiv) {
		if (stare != StareConcediu.SOLICITAT)
			throw new DomainException("Doar un concediu solicitat poate fi respins.");
		if (motiv == null || motiv.isBlank())
			throw new DomainException("Respingerea trebuie motivata.");
		motivRespingere = motiv;
		stare = StareConcediu.RESPINS;
	}

	public void anuleaza() {
		if (stare == StareConcediu.RESPINS || stare == StareConcediu.ANULAT)
			throw new DomainException("Concediul este deja " + stare);
		stare = StareConcediu.ANULAT;
	}

	public boolean esteActiv() {
		return stare == StareConcediu.SOLICITAT || stare == StareConcediu.APROBAT;
	}

	/** Regula A9: un angajat nu poate avea doua concedii active suprapuse. */
	public boolean seSuprapuneCu(Concediu altul) {
		return idAngajat.equals(altul.idAngajat) && esteActiv() && altul.esteActiv()
				&& perioada.seSuprapuneCu(altul.perioada);
	}

	/* ---- getteri ---- */
	public Integer getIdConcediu() { return idConcediu; }
	public Integer getIdAngajat() { return idAngajat; }
	public TipConcediu getTip() { return tip; }
	public Perioada getPerioada() { return perioada; }
	public StareConcediu getStare() { return stare; }
	public String getMotivRespingere() { return motivRespingere; }

	public void setIdConcediu(Integer idConcediu) {
		if (this.idConcediu != null)
			throw new DomainException("ID-ul nu se mai poate schimba.");
		this.idConcediu = idConcediu;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Concediu c && idConcediu != null && idConcediu.equals(c.idConcediu);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idConcediu);
	}

	@Override
	public String toString() {
		return "\nConcediu [id=" + idConcediu + ", angajat=" + idAngajat + ", " + tip + ", " + perioada + ", " + stare + "]";
	}
}
