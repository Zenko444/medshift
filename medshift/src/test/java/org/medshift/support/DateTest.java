package org.medshift.support;

import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.shared.GradMedical;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Personal de test pentru o sectie: 5 medici primari (101-105) si 5 asistenti (201-205). */
public final class DateTest {
	private DateTest() {
	}

	public static List<PersonalDisponibil> personalSectie() {
		List<PersonalDisponibil> p = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			p.add(new PersonalDisponibil(100 + i, GradMedical.MEDIC_PRIMAR, Set.of(), Set.of()));
			p.add(new PersonalDisponibil(200 + i, GradMedical.ASISTENT_MEDICAL, Set.of(), Set.of()));
		}
		return p;
	}

	public static List<PersonalDisponibil> personalCuDoleante(Integer idAngajat, Set<LocalDate> preferaLiber,
			Set<LocalDate> indisponibil) {
		return personalSectie().stream()
				.map(p -> p.idAngajat().equals(idAngajat)
						? new PersonalDisponibil(p.idAngajat(), p.grad(), indisponibil, preferaLiber) : p)
				.toList();
	}
}
