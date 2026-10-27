package org.medshift.services.personal;

import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.shared.LunaCalendaristica;

import java.util.List;

/**
 * Legatura dintre contextele Personal si Planificare (un "translator" intre ele):
 * transforma angajatii, doleantele si concediile in fisele PersonalDisponibil folosite la generarea graficului.
 */
public interface IDisponibilitateService {
	List<PersonalDisponibil> personalDisponibil(Integer idSectie, LunaCalendaristica luna);
}
