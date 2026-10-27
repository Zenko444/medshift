package org.medshift.services.personal;

import org.medshift.domain.personal.Angajat;
import org.medshift.domain.personal.Concediu;
import org.medshift.domain.personal.DoleanteLunare;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IValidarePersonalService {
	/** Erorile de validare JavaBean (adnotarile de pe campuri) pentru orice obiect din contextul Personal. */
	List<String> valideazaCampuri(Object entitate);
	/** Campuri + email unic. */
	List<String> valideazaAngajat(Angajat angajat);
	/** Campuri + angajatul exista + nu se suprapune cu alt concediu activ (A9). */
	List<String> valideazaConcediu(Concediu concediu);
	/** Campuri + angajatul exista + termenul de depunere nu a expirat (WF1). */
	List<String> valideazaDoleante(DoleanteLunare doleante, LocalDate termenDoleante, LocalDateTime acum);
}
