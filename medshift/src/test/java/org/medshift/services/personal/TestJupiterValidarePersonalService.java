package org.medshift.services.personal;

import org.junit.jupiter.api.Test;
import org.medshift.domain.personal.*;
import org.medshift.domain.shared.GradMedical;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.Perioada;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Validation Service din contextul Personal
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterValidarePersonalService {
	private static final Logger logger = Logger.getLogger(TestJupiterValidarePersonalService.class.getName());

	@Autowired private IValidarePersonalService validare;
	@Autowired private IAngajatFactory factory;
	@Autowired private IAngajatRepository angajati;
	@Autowired private IConcediuRepository concedii;

	@Test
	public void campuriLipsaSuntRaportate() {
		List<String> erori = validare.valideazaCampuri(new Angajat());
		logger.info("Erori JavaBean Validation: " + erori);
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("nume")));
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("email")));
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("contract")));
	}

	@Test
	public void emailulTrebuieSaFieUnic() {
		// medic1@ati.demo.ro exista deja: e creat la pornire de AngajatFactoryImpl.initPersonalDemo()
		Angajat dublura = factory.buildAngajatNormaIntreaga("Nou", "Medic", "medic1@ati.demo.ro",
				GradMedical.MEDIC_PRIMAR, "ATI", 1, LocalDate.of(2024, 1, 1));
		assertFalse(validare.valideazaAngajat(dublura).isEmpty());
	}

	@Test
	public void concediulSuprapusSiAngajatulInexistentSuntRespinse() {
		Angajat a = angajati.add(factory.buildAngajatNormaIntreaga("Validare", "Test", "validare.concediu@test.ro",
				GradMedical.MEDIC_SPECIALIST, "ATI", 2, LocalDate.of(2024, 1, 1)));
		concedii.add(new Concediu(null, a.getIdAngajat(), TipConcediu.ODIHNA,
				new Perioada(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10))));
		Concediu suprapus = new Concediu(concedii.getNextID(), a.getIdAngajat(), TipConcediu.ODIHNA,
				new Perioada(LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 12)));
		assertTrue(validare.valideazaConcediu(suprapus).stream().anyMatch(e -> e.contains("A9")));
		Concediu fantoma = new Concediu(concedii.getNextID(), 987654, TipConcediu.ODIHNA,
				new Perioada(LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 12)));
		assertFalse(validare.valideazaConcediu(fantoma).isEmpty());
	}

	@Test
	public void doleanteleDupaTermenSuntRespinse() {
		DoleanteLunare d = new DoleanteLunare(1, angajati.findBySectie(1).get(0).getIdAngajat(), new LunaCalendaristica(2026, 11));
		assertTrue(validare.valideazaDoleante(d, LocalDate.of(2026, 10, 20), LocalDateTime.of(2026, 10, 19, 9, 0)).isEmpty());
		assertFalse(validare.valideazaDoleante(d, LocalDate.of(2026, 10, 20), LocalDateTime.of(2026, 10, 21, 9, 0)).isEmpty());
	}
}
