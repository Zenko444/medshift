package org.medshift.services.pontaj;

import org.junit.jupiter.api.Test;
import org.medshift.domain.pontaj.CategorieOre;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.SegmentOre;
import org.medshift.domain.shared.CalendarSarbatoriLegale;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Computation Services din contextul Pontaj (segmentarea orelor + totaluri)
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterCalculPontajService {

	@Autowired private ISegmentareOreService segmentare;
	@Autowired private ICalculPontajService calcul;

	private Map<CategorieOre, Double> ore(TipTura tip, LocalDate data) {
		List<SegmentOre> s = segmentare.segmenteaza(tip.intervalPentru(data), tip);
		assertEquals(tip.intervalPentru(data).durataOre(), s.stream().mapToDouble(SegmentOre::ore).sum(),
				"segmentele acopera exact durata turei");
		return s.stream().collect(Collectors.toMap(SegmentOre::categorie, SegmentOre::ore));
	}

	@Test
	public void turaDeZiInTimpulSaptamanii() {
		assertEquals(Map.of(CategorieOre.ZI, 12.0), ore(TipTura.ZI, LocalDate.of(2026, 11, 5)));   // joi
	}

	@Test
	public void turaDeNoapteImpartitaPeOre() {
		// joi 20:00 -> vineri 08:00: 20-22 zi, 22-06 noapte, 06-08 zi
		assertEquals(Map.of(CategorieOre.ZI, 4.0, CategorieOre.NOAPTE, 8.0), ore(TipTura.NOAPTE, LocalDate.of(2026, 11, 5)));
		// vineri 20:00 -> sambata 08:00: orele de sambata sunt de weekend
		assertEquals(Map.of(CategorieOre.ZI, 2.0, CategorieOre.NOAPTE, 2.0, CategorieOre.WEEKEND, 8.0),
				ore(TipTura.NOAPTE, LocalDate.of(2026, 11, 6)));
	}

	@Test
	public void gardaSiSarbatoare() {
		// garda joi 08:00 -> vineri 08:00: 16 h de garda + 8 h de noapte
		assertEquals(Map.of(CategorieOre.GARDA, 16.0, CategorieOre.NOAPTE, 8.0), ore(TipTura.GARDA, LocalDate.of(2026, 11, 5)));
		// 1 Decembrie 2026 (marti) e sarbatoare legala
		assertEquals(Map.of(CategorieOre.SARBATOARE_LEGALA, 12.0), ore(TipTura.ZI, LocalDate.of(2026, 12, 1)));
	}

	@Test
	public void calendarulSarbatorilorCalculeazaPastele() {
		assertEquals(LocalDate.of(2025, 4, 20), CalendarSarbatoriLegale.pasteOrtodox(2025));
		assertEquals(LocalDate.of(2026, 4, 12), CalendarSarbatoriLegale.pasteOrtodox(2026));
		assertTrue(CalendarSarbatoriLegale.esteSarbatoare(LocalDate.of(2026, 4, 13)), "a doua zi de Paste");
		assertFalse(CalendarSarbatoriLegale.esteZiLucratoare(LocalDate.of(2026, 11, 7)), "sambata");
	}

	@Test
	public void totaluriSiOreSuplimentare() {
		FisaPontaj f = new FisaPontaj(1, 101, 1, new LunaCalendaristica(2026, 11));
		for (int zi = 2; zi <= 27; zi++) {                       // 26 de ture de zi la rand
			LocalDate d = LocalDate.of(2026, 11, zi);
			f.adaugaLinie(9000 + zi, d, TipTura.ZI, TipTura.ZI.intervalPentru(d),
					segmentare.segmenteaza(TipTura.ZI.intervalPentru(d), TipTura.ZI));
		}
		assertEquals(26 * 12.0, calcul.totaluri(f).total());
		assertEquals(26 * 12.0 - 168, calcul.oreSuplimentare(f, 168));
	}
}
