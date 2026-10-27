package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.PersonalDisponibil;

import java.util.List;

/**
 * Strategy Pattern (CF1.3): fiecare implementare e un bean Spring cu nume propriu.
 * Workflow-ul primeste toate strategiile ca Map<String, StrategieOptimizare> si o alege dupa nume.
 */
public interface StrategieOptimizare {
	/** Completeaza asignarile graficului (aflat in IN_AJUSTARE) folosind personalul disponibil. */
	void genereaza(GraficLunar grafic, List<PersonalDisponibil> personal);
}
