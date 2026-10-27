package org.medshift.services.planificare;

import org.junit.jupiter.api.Test;
import org.medshift.application.planificare.IPlanificareWorkflowService;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.support.CeasControlabil;
import org.medshift.support.DateTest;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Computation Services (livrabil Sprint 2)
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterCalculPlanificareService {
	private static final Logger logger = Logger.getLogger(TestJupiterCalculPlanificareService.class.getName());

	@Autowired
	private ICalculPlanificareService calcul;
	@Autowired
	private IPlanificareWorkflowService workflow;
	@Autowired
	private CeasControlabil ceas;

	@Test
	public void campuriSiViewuriCalculate() {
		ceas.seteaza(TestClockConfig.START);   // contextul Spring (si ceasul) e partajat intre clasele de test
		GraficLunar g = workflow.initializeazaGrafic("sef.sectie", 20, new LunaCalendaristica(2026, 11),
				LocalDate.of(2026, 10, 20), GraficLunarFactoryImpl.matriceStandard());
		assertEquals(0.0, calcul.gradAcoperireGrafic(g), "inainte de generare nimic nu e acoperit");
		assertEquals(60, calcul.raportTureSubNecesar(g).size(), "30 zile x 2 ture");

		workflow.genereazaGrafic("sef.sectie", g.getIdGrafic(), "echitate", DateTest.personalSectie());

		assertEquals(1.0, calcul.gradAcoperireGrafic(g));
		assertTrue(calcul.raportTureSubNecesar(g).isEmpty());
		Integer primaTura = g.getTure().get(0).getIdTura();
		assertEquals(1.0, calcul.gradAcoperire(g, primaTura));

		List<IndicatoriAngajat> echitate = calcul.raportEchitate(g);
		echitate.forEach(i -> logger.info("Echitate: " + i));
		assertEquals(10, echitate.size());
		// fiecare tura are 12 h, deci orele planificate = 12 x numarul de ture
		echitate.forEach(i -> assertEquals(12.0 * i.totalTure(), i.orePlanificate()));
		// 60 ture x 2 persoane = 120 asignari impartite la 10 oameni
		assertEquals(120, echitate.stream().mapToLong(IndicatoriAngajat::totalTure).sum());
		assertTrue(calcul.diferentaMaximaTureGrele(g) <= 2, "strategia de echitate tine diferenta mica");
	}
}
