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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Computation Service din contextul Personal
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterCalculPersonalService {

	@Autowired private ICalculPersonalService calcul;
	@Autowired private IAngajatFactory factory;
	@Autowired private IAngajatRepository angajati;
	@Autowired private IConcediuRepository concedii;

	@Test
	public void zileLucratoareDeConcediuFaraWeekendSiSarbatori() {
		Angajat a = angajati.add(factory.buildAngajatNormaIntreaga("Calcul", "Test", "calcul.concediu@test.ro",
				GradMedical.ASISTENT_MEDICAL, "ATI", 3, LocalDate.of(2024, 1, 1)));
		// 23.11 - 04.12.2026: 10 zile luni-vineri, dar 30.11 si 01.12 sunt sarbatori legale => 8 zile lucratoare
		Concediu c = concedii.add(new Concediu(null, a.getIdAngajat(), TipConcediu.ODIHNA,
				new Perioada(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 12, 4))));
		assertEquals(0, calcul.zileLucratoareConcediuOdihna(a.getIdAngajat(), 2026), "un concediu doar solicitat nu se numara");
		c.aproba();
		assertEquals(8, calcul.zileLucratoareConcediuOdihna(a.getIdAngajat(), 2026));
		assertEquals(CalculPersonalServiceImpl.ZILE_CONCEDIU_ODIHNA_PE_AN - 8, calcul.zileConcediuOdihnaRamase(a.getIdAngajat(), 2026));
	}

	@Test
	public void procentulDoleantelorRespectate() {
		DoleanteLunare d = new DoleanteLunare(1, 1, new LunaCalendaristica(2026, 11));
		LocalDateTime inainte = LocalDateTime.of(2026, 10, 1, 9, 0);
		d.adaugaPreferinta(new PreferintaZi(LocalDate.of(2026, 11, 7), TipPreferinta.PREFER_LIBER, null), LocalDate.of(2026, 10, 20), inainte);
		d.adaugaPreferinta(new PreferintaZi(LocalDate.of(2026, 11, 8), TipPreferinta.PREFER_LIBER, null), LocalDate.of(2026, 10, 20), inainte);
		assertEquals(50.0, calcul.procentDoleanteRespectate(d, Set.of(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 12))));
		assertEquals(100.0, calcul.procentDoleanteRespectate(new DoleanteLunare(2, 1, new LunaCalendaristica(2026, 11)), Set.of()));
	}
}
