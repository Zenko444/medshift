package org.medshift.domain.planificare;

import org.medshift.domain.shared.GradMedical;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

/**
 * Value Object (read model): ce trebuie sa stie Planificarea despre un angajat ca sa-l poata pune pe ture.
 * Datele vin din contextul Personal (profil, doleante, concedii aprobate), dar Planificarea
 * nu depinde direct de clasele de acolo: aceasta e "traducerea" intre contexte.
 */
public record PersonalDisponibil(
		@NotNull Integer idAngajat,
		@NotNull GradMedical grad,
		Set<LocalDate> zileIndisponibile,   // concedii aprobate + indisponibilitati declarate (restrictie dura)
		Set<LocalDate> zilePreferaLiber) {  // doleante "prefer liber" (restrictie soft)

	public PersonalDisponibil {
		zileIndisponibile = zileIndisponibile == null ? Set.of() : Set.copyOf(zileIndisponibile);
		zilePreferaLiber = zilePreferaLiber == null ? Set.of() : Set.copyOf(zilePreferaLiber);
	}

	public boolean esteDisponibil(LocalDate data) {
		return !zileIndisponibile.contains(data);
	}

	public boolean preferaLiber(LocalDate data) {
		return zilePreferaLiber.contains(data);
	}
}
