package org.medshift.application.pontaj;

import org.junit.jupiter.api.Test;
import org.medshift.application.planificare.IPlanificareWorkflowService;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.pontaj.*;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.planificare.GraficLunarFactoryImpl;
import org.medshift.services.pontaj.ExportBorderouServiceImpl;
import org.medshift.services.pontaj.ICalculPontajService;
import org.medshift.support.CeasControlabil;
import org.medshift.support.DateTest;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Workflow Service din contextul Pontaj - de la graficul finalizat la exportul borderoului
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterPontajWorkflowService {
	private static final Logger logger = Logger.getLogger(TestJupiterPontajWorkflowService.class.getName());

	@Autowired private IPontajWorkflowService pontaj;
	@Autowired private IPlanificareWorkflowService planificare;
	@Autowired private ICalculPontajService calcul;
	@Autowired private CeasControlabil ceas;

	private static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);

	private GraficLunar graficFinalizat(int sectie) {
		ceas.seteaza(TestClockConfig.START);
		GraficLunar g = planificare.initializeazaGrafic("sef.sectie", sectie, NOIEMBRIE, LocalDate.of(2026, 10, 20),
				GraficLunarFactoryImpl.matriceStandard());
		planificare.genereazaGrafic("sef.sectie", g.getIdGrafic(), "echitate", DateTest.personalSectie());
		planificare.publicaGrafic("sef.sectie", g.getIdGrafic());
		ceas.seteaza(LocalDateTime.of(2026, 12, 1, 0, 30));
		planificare.finalizeazaGrafic("sistem", g.getIdGrafic());
		return g;
	}

	@Test
	public void delaGraficFinalizatLaBorderouExportat() {
		GraficLunar g = graficFinalizat(70);

		List<FisaPontaj> fise = pontaj.genereazaFise("hr", g.getIdGrafic());
		assertEquals(10, fise.size(), "o fisa pentru fiecare dintre cei 10 angajati");
		assertThrows(DomainException.class, () -> pontaj.genereazaFise("hr", g.getIdGrafic()), "nu se genereaza de doua ori");
		assertEquals(30 * 2 * 2 * 12.0, calcul.totalSectie(70, NOIEMBRIE).total(), "60 de ture x 2 oameni x 12 h");
		assertThrows(DomainException.class, () -> pontaj.genereazaBorderou("hr", 70, NOIEMBRIE, FormatExport.CSV),
				"borderoul cere toate fisele aprobate");

		List<FisaPontaj> cuProbleme = pontaj.verificaToateFisele("hr", 70, NOIEMBRIE);
		assertTrue(cuProbleme.isEmpty(), "un grafic generat corect produce fise conforme: " + cuProbleme);
		fise.forEach(f -> pontaj.aprobaFisa("hr", f.getIdFisa()));

		BorderouSalarial b = pontaj.genereazaBorderou("hr", 70, NOIEMBRIE, FormatExport.CSV);
		String csv = pontaj.exportaBorderou("hr", b.getIdBorderou());
		logger.info("CSV exportat:\n" + csv);
		assertTrue(csv.startsWith(ExportBorderouServiceImpl.ANTET_CSV));
		assertEquals(11, csv.lines().count(), "antet + 10 angajati");
		assertTrue(fise.stream().allMatch(f -> f.getStare() == StareFisa.EXPORTATA));
		assertEquals(csv, pontaj.exportaBorderou("hr", b.getIdBorderou()), "al doilea export da acelasi fisier (R5)");
	}

	@Test
	public void fisaRespinsaDeHrRevineLaCorectare() {
		GraficLunar g = graficFinalizat(71);
		FisaPontaj f = pontaj.genereazaFise("hr", g.getIdGrafic()).get(0);
		pontaj.verificaFisa("hr", f.getIdFisa());
		pontaj.respingeFisa("hr", f.getIdFisa(), "Lipseste o tura inlocuita");
		assertEquals(StareFisa.NECONFORMA, f.getStare());
		assertTrue(pontaj.verificaFisa("hr", f.getIdFisa()).isEmpty());
		assertEquals(StareFisa.VALIDATA_LEGAL, f.getStare());
	}

	@Test
	public void fiseleNuSeGenereazaDintrUnGraficNefinalizat() {
		ceas.seteaza(TestClockConfig.START);
		GraficLunar g = planificare.initializeazaGrafic("sef.sectie", 72, NOIEMBRIE, LocalDate.of(2026, 10, 20),
				GraficLunarFactoryImpl.matriceStandard());
		assertThrows(DomainException.class, () -> pontaj.genereazaFise("hr", g.getIdGrafic()));
	}
}
