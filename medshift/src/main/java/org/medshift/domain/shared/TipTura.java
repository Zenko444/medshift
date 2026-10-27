package org.medshift.domain.shared;

import java.time.LocalDate;
import java.time.LocalTime;

/** Tipul turei si orarul ei standard. Noaptea si garda trec in ziua urmatoare. */
public enum TipTura {
	ZI(LocalTime.of(8, 0), 12),
	NOAPTE(LocalTime.of(20, 0), 12),
	GARDA(LocalTime.of(8, 0), 24);

	private final LocalTime oraInceput;
	private final int durataOre;

	TipTura(LocalTime oraInceput, int durataOre) {
		this.oraInceput = oraInceput;
		this.durataOre = durataOre;
	}

	public IntervalOrar intervalPentru(LocalDate data) {
		var inceput = data.atTime(oraInceput);
		return new IntervalOrar(inceput, inceput.plusHours(durataOre));
	}

	/** Pauza minima obligatorie dupa acest tip de tura (regula A3). */
	public int pauzaMinimaOre() {
		return this == GARDA ? 24 : 12;
	}
}
