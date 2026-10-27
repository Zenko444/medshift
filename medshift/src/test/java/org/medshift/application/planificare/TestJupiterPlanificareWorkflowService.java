package org.medshift.application.planificare;

import org.junit.jupiter.api.Test;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.StareGrafic;
import org.medshift.domain.planificare.StareTura;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.audit.IAuditService;
import org.medshift.services.planificare.GraficLunarFactoryImpl;
import org.medshift.support.CeasControlabil;
import org.medshift.support.DateTest;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Workflow Services (livrabil Sprint 2) - ciclul complet al graficului lunar
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterPlanificareWorkflowService {
	private static final Logger logger = Logger.getLogger(TestJupiterPlanificareWorkflowService.class.getName());

	@Autowired
	private IPlanificareWorkflowService workflow;
	@Autowired
	private IAuditService audit;
	@Autowired
	private CeasControlabil ceas;

	private GraficLunar initializeaza(int idSectie) {
		ceas.seteaza(TestClockConfig.START);
		return workflow.initializeazaGrafic("sef.sectie", idSectie, new LunaCalendaristica(2026, 11),
				LocalDate.of(2026, 10, 20), GraficLunarFactoryImpl.matriceStandard());
	}

	@Test
	public void ambeleStrategiiSuntInjectate() {
		logger.info("Strategii disponibile: " + workflow.strategiiDisponibile());
		assertEquals(Set.of("echitate", "prioritateDoleante"), workflow.strategiiDisponibile());
	}

	@Test
	public void cicluCompletInitializareGenerarePublicareFinalizare() {
		GraficLunar g = initializeaza(30);
		assertEquals(StareGrafic.IN_COLECTARE_DOLEANTE, g.getStare());

		List<String> probleme = workflow.genereazaGrafic("sef.sectie", g.getIdGrafic(), "echitate", DateTest.personalSectie());
		assertTrue(probleme.isEmpty(), "graficul generat respecta toate restrictiile: " + probleme);
		assertEquals(StareGrafic.IN_AJUSTARE, g.getStare());

		workflow.publicaGrafic("sef.sectie", g.getIdGrafic());
		assertEquals(StareGrafic.PUBLICAT, g.getStare());

		ceas.seteaza(TestClockConfig.START.plusDays(4));   // dupa fereastra de 3 zile
		assertEquals(120, workflow.aplicaConfirmariTacite(g.getIdGrafic()));

		ceas.seteaza(LocalDateTime.of(2026, 12, 1, 0, 30));
		workflow.finalizeazaGrafic("sistem", g.getIdGrafic());
		assertEquals(StareGrafic.FINALIZAT, g.getStare());

		List<String> actiuni = audit.istoricPentru("GraficLunar#" + g.getIdGrafic()).stream()
				.map(a -> a.actiune()).toList();
		logger.info("Audit: " + actiuni);
		assertEquals(List.of("INITIALIZARE", "GENERARE", "PUBLICARE", "CONFIRMARE_TACITA", "FINALIZARE"), actiuni);
	}

	@Test
	public void strategiaPrioritateDoleanteRespectaZileleLibere() {
		GraficLunar g = initializeaza(31);
		Set<LocalDate> liber = Set.of(LocalDate.of(2026, 11, 7), LocalDate.of(2026, 11, 8));
		workflow.genereazaGrafic("sef.sectie", g.getIdGrafic(), "prioritateDoleante",
				DateTest.personalCuDoleante(101, liber, Set.of()));
		assertTrue(g.tureAngajat(101).stream().noneMatch(t -> liber.contains(t.getData())),
				"medicul 101 nu lucreaza in zilele cerute libere");
	}

	@Test
	public void concediulLasaTureleAngajatuluiDescoperite() {
		GraficLunar g = initializeaza(32);
		workflow.genereazaGrafic("sef.sectie", g.getIdGrafic(), "echitate", DateTest.personalSectie());
		workflow.publicaGrafic("sef.sectie", g.getIdGrafic());

		List<Tura> afectate = workflow.inregistreazaConcediu("hr", g.getIdGrafic(), 101,
				LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14));
		assertFalse(afectate.isEmpty());
		assertTrue(afectate.stream().allMatch(t -> t.getStare() == StareTura.DESCOPERITA));
		assertTrue(g.tureAngajat(101).stream()
				.noneMatch(t -> !t.getData().isBefore(LocalDate.of(2026, 11, 10)) && !t.getData().isAfter(LocalDate.of(2026, 11, 14))));
	}

	@Test
	public void strategieNecunoscutaSiLunaTrecutaSuntRespinse() {
		GraficLunar g = initializeaza(33);
		assertThrows(DomainException.class,
				() -> workflow.genereazaGrafic("sef.sectie", g.getIdGrafic(), "aleator", DateTest.personalSectie()));
		assertThrows(DomainException.class, () -> workflow.initializeazaGrafic("sef.sectie", 34,
				new LunaCalendaristica(2026, 10), LocalDate.of(2026, 9, 20), GraficLunarFactoryImpl.matriceStandard()));
	}

	@Test
	public void unSingurGraficPeSectieSiLuna() {
		initializeaza(35);
		assertThrows(DomainException.class, () -> initializeaza(35));
	}
}
