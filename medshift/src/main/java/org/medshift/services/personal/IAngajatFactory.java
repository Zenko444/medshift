package org.medshift.services.personal;

import org.medshift.domain.personal.Angajat;
import org.medshift.domain.personal.Contract;
import org.medshift.domain.shared.GradMedical;

import java.time.LocalDate;

public interface IAngajatFactory {
	// angajat cu contractul dat; ID-ul il primeste de la repository
	Angajat buildAngajat(String nume, String prenume, String email, GradMedical grad, String specializare,
			Integer idSectie, Contract contract);
	// cazul cel mai frecvent: norma intreaga, perioada nedeterminata, cu garzi
	Angajat buildAngajatNormaIntreaga(String nume, String prenume, String email, GradMedical grad,
			String specializare, Integer idSectie, LocalDate dataAngajarii);
	IAngajatRepository getAngajatRepository();
}
