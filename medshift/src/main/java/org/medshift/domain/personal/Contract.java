package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

/** Entitate in agregatul Angajat: contractul de munca (norma, ore, daca face garzi). */
public class Contract implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int NORMA_INTREAGA_ORE_LUNARE = 168;

	@NotNull(message = "Tipul normei este obligatoriu.")
	private TipNorma tipNorma;
	@Min(value = 1, message = "Norma lunara trebuie sa fie pozitiva.")
	@Max(value = NORMA_INTREAGA_ORE_LUNARE, message = "Norma lunara nu poate depasi norma intreaga.")
	private int normaOreLunare;
	private boolean includeGarzi;
	@NotNull(message = "Contractul trebuie sa aiba data de inceput.")
	private LocalDate dataInceput;
	private LocalDate dataSfarsit;          // null = perioada nedeterminata

	public Contract() {
	}

	public Contract(TipNorma tipNorma, int normaOreLunare, boolean includeGarzi, LocalDate dataInceput, LocalDate dataSfarsit) {
		if (tipNorma == null || dataInceput == null)
			throw new DomainException("Contractul trebuie sa aiba tipul normei si data de inceput.");
		if (normaOreLunare <= 0 || normaOreLunare > NORMA_INTREAGA_ORE_LUNARE)
			throw new DomainException("Norma lunara trebuie sa fie intre 1 si " + NORMA_INTREAGA_ORE_LUNARE + " ore.");
		if (tipNorma == TipNorma.PARTIALA && normaOreLunare >= NORMA_INTREAGA_ORE_LUNARE)   // regula V4
			throw new DomainException("La norma partiala, orele lunare trebuie sa fie sub norma intreaga.");
		if (dataSfarsit != null && dataSfarsit.isBefore(dataInceput))
			throw new DomainException("Contractul nu se poate termina inainte sa inceapa.");
		this.tipNorma = tipNorma;
		this.normaOreLunare = normaOreLunare;
		this.includeGarzi = includeGarzi;
		this.dataInceput = dataInceput;
		this.dataSfarsit = dataSfarsit;
	}

	public static Contract normaIntreaga(LocalDate dataInceput, boolean includeGarzi) {
		return new Contract(TipNorma.INTREAGA, NORMA_INTREAGA_ORE_LUNARE, includeGarzi, dataInceput, null);
	}

	public boolean esteActivLa(LocalDate data) {
		return !data.isBefore(dataInceput) && (dataSfarsit == null || !data.isAfter(dataSfarsit));
	}

	public TipNorma getTipNorma() { return tipNorma; }
	public int getNormaOreLunare() { return normaOreLunare; }
	public boolean isIncludeGarzi() { return includeGarzi; }
	public LocalDate getDataInceput() { return dataInceput; }
	public LocalDate getDataSfarsit() { return dataSfarsit; }

	@Override
	public String toString() {
		return "Contract [" + tipNorma + ", " + normaOreLunare + " h/luna, garzi=" + includeGarzi + "]";
	}
}
