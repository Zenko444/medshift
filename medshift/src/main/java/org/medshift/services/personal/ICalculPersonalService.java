package org.medshift.services.personal;

import org.medshift.domain.personal.DoleanteLunare;

import java.time.LocalDate;
import java.util.Set;

public interface ICalculPersonalService {
	// Computed entity fields
	long zileLucratoareConcediuOdihna(Integer idAngajat, int an);   // zile luate sau aprobate in anul dat
	long zileConcediuOdihnaRamase(Integer idAngajat, int an);
	// Computed entity views
	/** Procentul zilelor "prefer liber" in care angajatul chiar nu a lucrat (0-100; 100 daca nu a cerut nimic). */
	double procentDoleanteRespectate(DoleanteLunare doleante, Set<LocalDate> zileLucrate);
}
