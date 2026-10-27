package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * Aggregate Root (Personal): o cerere de schimb de tura Peer-to-Peer (CF2.3).
 * Tine doar ciclul de viata al cererii; schimbul efectiv il face GraficLunar.schimbaAsignari(...).
 */
public class CerereSchimbTura implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer idCerere;
	@NotNull private Integer idGrafic;
	@NotNull private Integer idSolicitant;
	@NotNull private Integer idColeg;
	@NotNull private Integer idTuraOferita;      // tura solicitantului
	@NotNull private Integer idTuraSolicitata;   // tura colegului
	@NotNull private StareSchimb stare = StareSchimb.INITIATA;
	@Size(max = 255) private String motivRespingere;

	public CerereSchimbTura() {
	}

	public CerereSchimbTura(Integer idCerere, Integer idGrafic, Integer idSolicitant, Integer idColeg,
			Integer idTuraOferita, Integer idTuraSolicitata) {
		this.idCerere = idCerere;
		this.idGrafic = Objects.requireNonNull(idGrafic, "idGrafic");
		this.idSolicitant = Objects.requireNonNull(idSolicitant, "idSolicitant");
		this.idColeg = Objects.requireNonNull(idColeg, "idColeg");
		this.idTuraOferita = Objects.requireNonNull(idTuraOferita, "idTuraOferita");
		this.idTuraSolicitata = Objects.requireNonNull(idTuraSolicitata, "idTuraSolicitata");
		if (idSolicitant.equals(idColeg))
			throw new DomainException("Nu poti face schimb de tura cu tine insuti.");
		if (idTuraOferita.equals(idTuraSolicitata))
			throw new DomainException("Turele schimbate trebuie sa fie diferite.");
	}

	public void acceptaDeColeg() {
		cereStare(StareSchimb.INITIATA);
		stare = StareSchimb.ACCEPTATA_DE_COLEG;
	}

	/** Cand schimbul nu poate fi aprobat automat (ex.: grade diferite), merge la seful de sectie. */
	public void trimiteLaAprobare() {
		cereStare(StareSchimb.ACCEPTATA_DE_COLEG);
		stare = StareSchimb.IN_APROBARE;
	}

	public void aproba() {
		cereStare(StareSchimb.ACCEPTATA_DE_COLEG, StareSchimb.IN_APROBARE);
		stare = StareSchimb.APROBATA;
	}

	public void respinge(String motiv) {
		cereStare(StareSchimb.INITIATA, StareSchimb.ACCEPTATA_DE_COLEG, StareSchimb.IN_APROBARE);
		if (motiv == null || motiv.isBlank())
			throw new DomainException("Respingerea trebuie motivata.");
		motivRespingere = motiv;
		stare = StareSchimb.RESPINSA;
	}

	public void anuleaza() {
		cereStare(StareSchimb.INITIATA, StareSchimb.ACCEPTATA_DE_COLEG, StareSchimb.IN_APROBARE);
		stare = StareSchimb.ANULATA;
	}

	public boolean esteDeschisa() {
		return stare == StareSchimb.INITIATA || stare == StareSchimb.ACCEPTATA_DE_COLEG || stare == StareSchimb.IN_APROBARE;
	}

	private void cereStare(StareSchimb... permise) {
		for (StareSchimb s : permise)
			if (stare == s)
				return;
		throw new DomainException("Operatia nu este permisa pentru o cerere in starea " + stare);
	}

	/* ---- getteri ---- */
	public Integer getIdCerere() { return idCerere; }
	public Integer getIdGrafic() { return idGrafic; }
	public Integer getIdSolicitant() { return idSolicitant; }
	public Integer getIdColeg() { return idColeg; }
	public Integer getIdTuraOferita() { return idTuraOferita; }
	public Integer getIdTuraSolicitata() { return idTuraSolicitata; }
	public StareSchimb getStare() { return stare; }
	public String getMotivRespingere() { return motivRespingere; }

	public void setIdCerere(Integer idCerere) {
		if (this.idCerere != null)
			throw new DomainException("ID-ul nu se mai poate schimba.");
		this.idCerere = idCerere;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof CerereSchimbTura c && idCerere != null && idCerere.equals(c.idCerere);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idCerere);
	}

	@Override
	public String toString() {
		return "\nCerereSchimbTura [id=" + idCerere + ", " + idSolicitant + "@" + idTuraOferita + " <-> "
				+ idColeg + "@" + idTuraSolicitata + ", " + stare + "]";
	}
}
