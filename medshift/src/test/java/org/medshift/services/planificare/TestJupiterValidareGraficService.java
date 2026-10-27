package org.medshift.services.planificare;

import org.junit.jupiter.api.Test;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Validation Service (livrabil Sprint 2)
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterValidareGraficService {
	private static final Logger logger = Logger.getLogger(TestJupiterValidareGraficService.class.getName());

	@Autowired
	private IValidareGraficService validare;
	@Autowired
	private IGraficLunarFactory factory;

	@Test
	public void graficNouAreCampuriValideDarNuPoateFiPublicat() {
		GraficLunar g = factory.buildGraficCuTure(7, new LunaCalendaristica(2026, 12), LocalDate.of(2026, 11, 20),
				Set.of(TipTura.ZI, TipTura.NOAPTE), GraficLunarFactoryImpl.matriceStandard());
		assertTrue(validare.valideazaCampuri(g).isEmpty(), "campurile adnotate sunt completate corect");
		List<String> restrictii = validare.valideazaRestrictii(g);
		logger.info("Restrictii incalcate (grafic fara asignari): " + restrictii.size());
		assertEquals(31 * 2 * 2, restrictii.size(), "31 zile x 2 ture x 2 grade sub necesar (A1)");
		assertFalse(validare.poateFiPublicat(g));
	}

	@Test
	public void validatorulJavaBeanGasesteCampLipsa() {
		GraficLunar g = new GraficLunar();   // constructorul fara argumente (folosit de JPA) lasa campurile goale
		List<String> erori = validare.valideazaCampuri(g);
		logger.info("Erori JavaBean Validation: " + erori);
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("idSectie")));
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("luna")));
		assertTrue(erori.stream().anyMatch(e -> e.startsWith("termenDoleante")));
	}
}
