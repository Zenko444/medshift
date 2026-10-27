package org.medshift.services.personal;

import jakarta.annotation.PostConstruct;
import org.medshift.domain.personal.*;
import org.medshift.domain.shared.GradMedical;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.logging.Logger;

// Aggregate Entity Factory pentru Angajat (livrabil Sprint 2).
@Component
public class AngajatFactoryImpl implements IAngajatFactory {
	private static final Logger logger = Logger.getLogger(AngajatFactoryImpl.class.getName());

	/** Aceeasi sectie ca graficul demo din GraficLunarFactoryImpl. */
	public static final Integer SECTIE_ATI = 1;

	private final IAngajatRepository repository;

	public AngajatFactoryImpl(IAngajatRepository repository) {
		this.repository = repository;
		logger.info(">>> BEAN: AngajatFactoryImpl instantiated!");
	}

	@Override
	public Angajat buildAngajat(String nume, String prenume, String email, GradMedical grad, String specializare,
			Integer idSectie, Contract contract) {
		return new Angajat(repository.getNextID(), new NumeComplet(nume, prenume), new Email(email), grad,
				specializare, idSectie, contract);
	}

	@Override
	public Angajat buildAngajatNormaIntreaga(String nume, String prenume, String email, GradMedical grad,
			String specializare, Integer idSectie, LocalDate dataAngajarii) {
		return buildAngajat(nume, prenume, email, grad, specializare, idSectie,
				Contract.normaIntreaga(dataAngajarii, true));
	}

	@Override
	public IAngajatRepository getAngajatRepository() {
		return repository;
	}

	// Date de test: echipa sectiei ATI (5 medici primari + 5 asistenti), ca initDomainServiceEntities din T2.C3
	@PostConstruct
	public void initPersonalDemo() {
		String[] numeMedici = {"Popa", "Ionescu", "Marin", "Dobre", "Stan"};
		String[] numeAsistenti = {"Rusu", "Lungu", "Neagu", "Toma", "Matei"};
		LocalDate angajare = LocalDate.of(2020, 1, 1);
		for (int i = 0; i < 5; i++) {
			Angajat medic = buildAngajatNormaIntreaga(numeMedici[i], "Medic", "medic" + (i + 1) + "@ati.demo.ro",
					GradMedical.MEDIC_PRIMAR, "ATI", SECTIE_ATI, angajare);
			medic.adaugaCompetenta(new Competenta("VENT", "Ventilatie mecanica"));
			repository.add(medic);
			repository.add(buildAngajatNormaIntreaga(numeAsistenti[i], "Asistent", "asistent" + (i + 1) + "@ati.demo.ro",
					GradMedical.ASISTENT_MEDICAL, "ATI", SECTIE_ATI, angajare));
		}
		logger.info(">> PostConstruct :: angajati demo in sectia ATI: " + repository.findBySectie(SECTIE_ATI).size());
	}
}
