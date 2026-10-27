package org.medshift.services.pontaj;

import org.junit.jupiter.api.Test;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.Neconformitate;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Validation Service din contextul Pontaj (verificarea legala)
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterConformitateService {
	private static final Logger logger = Logger.getLogger(TestJupiterConformitateService.class.getName());

	@Autowired private IConformitateService conformitate;
	@Autowired private ISegmentareOreService segmentare;

	private static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);

	private void tura(FisaPontaj f, int idTura, LocalDate zi, TipTura tip) {
		f.adaugaLinie(idTura, zi, tip, tip.intervalPentru(zi), segmentare.segmenteaza(tip.intervalPentru(zi), tip));
	}

	@Test
	public void fisaCorectaNuAreNeconformitati() {
		FisaPontaj f = new FisaPontaj(1, 101, 1, NOIEMBRIE);
		tura(f, 1, LocalDate.of(2026, 11, 2), TipTura.ZI);
		tura(f, 2, LocalDate.of(2026, 11, 4), TipTura.NOAPTE);
		tura(f, 3, LocalDate.of(2026, 11, 7), TipTura.GARDA);
		assertTrue(conformitate.verifica(f).isEmpty());
	}

	@Test
	public void pauzaPreaScurtaDupaGarda_A3() {
		FisaPontaj f = new FisaPontaj(2, 102, 1, NOIEMBRIE);
		tura(f, 1, LocalDate.of(2026, 11, 5), TipTura.GARDA);   // joi 08:00 -> vineri 08:00
		tura(f, 2, LocalDate.of(2026, 11, 6), TipTura.ZI);      // vineri 08:00: 0 h pauza
		List<Neconformitate> n = conformitate.verifica(f);
		logger.info("Neconformitati: " + n);
		assertEquals(1, n.size());
		assertEquals("A3", n.get(0).regula());
	}

	@Test
	public void preaMulteOrePeSaptamanaInMedie_CM114() {
		FisaPontaj f = new FisaPontaj(3, 103, 1, NOIEMBRIE);
		for (int zi = 1; zi <= 20; zi++)                         // 20 x 12 h = 240 h / 4,3 saptamani = 56 h
			tura(f, zi, LocalDate.of(2026, 11, zi), TipTura.ZI);
		assertTrue(conformitate.verifica(f).stream().anyMatch(n -> n.regula().equals("CM-114")));
	}
}
