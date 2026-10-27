package org.medshift.domain.personal;

import org.junit.jupiter.api.Test;
import org.medshift.domain.shared.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: regulile entitatilor din contextul Personal (fara Spring)
public class TestJupiterEntitatiPersonal {

	static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);
	static final LocalDate TERMEN = LocalDate.of(2026, 10, 20);

	@Test
	void valueObjectsValideaza() {
		assertThrows(DomainException.class, () -> new Email("nu-e-email"));
		assertEquals("ana@arcadia.ro", new Email("  Ana@Arcadia.RO ").adresa());
		assertThrows(DomainException.class, () -> new NumeComplet("A", "Ana"));
		assertThrows(DomainException.class, () -> new Perioada(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)));
		assertEquals(5, new Perioada(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14)).numarZile());
		assertThrows(DomainException.class, () -> new Contract(TipNorma.PARTIALA, 168, false, LocalDate.of(2026, 1, 1), null));
	}

	@Test
	void angajatEligibilDupaGradCompetenteSiContract() {
		Angajat a = new Angajat(1, new NumeComplet("Popescu", "Ana"), new Email("ana@arcadia.ro"), GradMedical.MEDIC_PRIMAR,
				"ATI", 1, new Contract(TipNorma.INTREAGA, 168, true, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 11, 15)));
		a.adaugaCompetenta(new Competenta("vent", "Ventilatie mecanica"));
		LocalDate zi = LocalDate.of(2026, 11, 5);
		assertTrue(a.esteEligibilPentru(GradMedical.MEDIC_PRIMAR, Set.of("VENT"), zi));
		assertFalse(a.esteEligibilPentru(GradMedical.MEDIC_PRIMAR, Set.of("ECO"), zi), "lipseste competenta");
		assertFalse(a.esteEligibilPentru(GradMedical.ASISTENT_MEDICAL, Set.of(), zi), "alt grad");
		assertFalse(a.esteEligibilPentru(GradMedical.MEDIC_PRIMAR, Set.of(), LocalDate.of(2026, 11, 20)), "contract expirat");
	}

	@Test
	void doleanteDoarInLunaSiPanaLaTermen() {
		DoleanteLunare d = new DoleanteLunare(1, 101, NOIEMBRIE);
		LocalDateTime inainteDeTermen = LocalDateTime.of(2026, 10, 15, 10, 0);
		d.adaugaPreferinta(new PreferintaZi(LocalDate.of(2026, 11, 7), TipPreferinta.PREFER_LIBER, null), TERMEN, inainteDeTermen);
		d.adaugaPreferinta(new PreferintaZi(LocalDate.of(2026, 11, 7), TipPreferinta.INDISPONIBIL, null), TERMEN, inainteDeTermen);
		assertEquals(1, d.getPreferinte().size(), "o singura doleanta pe zi, ultima o inlocuieste");
		assertEquals(Set.of(LocalDate.of(2026, 11, 7)), d.zileIndisponibile());
		assertThrows(DomainException.class, () -> d.adaugaPreferinta(
				new PreferintaZi(LocalDate.of(2026, 12, 1), TipPreferinta.PREFER_LIBER, null), TERMEN, inainteDeTermen));
		assertThrows(DomainException.class, () -> d.adaugaPreferinta(
				new PreferintaZi(LocalDate.of(2026, 11, 8), TipPreferinta.PREFER_LIBER, null), TERMEN, LocalDateTime.of(2026, 10, 21, 9, 0)));
	}

	@Test
	void concediuSiCerereDeSchimbAuStarileCorecte() {
		Concediu urgenta = new Concediu(1, 101, TipConcediu.URGENTA_MEDICALA,
				new Perioada(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 4)));
		assertEquals(StareConcediu.APROBAT, urgenta.getStare(), "urgenta medicala e aprobata automat");
		Concediu odihna = new Concediu(2, 101, TipConcediu.ODIHNA, new Perioada(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 25)));
		assertThrows(DomainException.class, () -> odihna.respinge(" "), "respingerea trebuie motivata");

		CerereSchimbTura c = new CerereSchimbTura(1, 1, 101, 102, 1001, 1003);
		assertThrows(DomainException.class, c::aproba, "nu se aproba inainte sa accepte colegul");
		c.acceptaDeColeg();
		c.trimiteLaAprobare();
		c.aproba();
		assertEquals(StareSchimb.APROBATA, c.getStare());
		assertThrows(DomainException.class, () -> new CerereSchimbTura(2, 1, 101, 101, 1001, 1003));
	}
}
