package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: testeaza invariantii agregatului GraficLunar (fara Spring)
public class TestJupiterGraficLunar {

	static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);
	static final LocalDate TERMEN = LocalDate.of(2026, 10, 20);
	static final LocalDateTime PUBLICARE = LocalDateTime.of(2026, 10, 25, 9, 0);
	static final Integer POPESCU = 1, IONESCU = 2, MARIN = 3;

	GraficLunar grafic;

	@BeforeEach
	void setUp() {
		grafic = new GraficLunar(1, 10, NOIEMBRIE, TERMEN);
		grafic.configureazaNecesar(new MatriceAcoperire(List.of(
				new CerintaAcoperire(TipTura.ZI, GradMedical.MEDIC_PRIMAR, 1),
				new CerintaAcoperire(TipTura.NOAPTE, GradMedical.MEDIC_PRIMAR, 1))));
		grafic.adaugaTura(101, LocalDate.of(2026, 11, 5), TipTura.ZI);
		grafic.adaugaTura(102, LocalDate.of(2026, 11, 5), TipTura.NOAPTE);
		grafic.adaugaTura(103, LocalDate.of(2026, 11, 6), TipTura.ZI);
	}

	@Test
	void graficNouEsteInColectareDoleante() {
		assertEquals(StareGrafic.IN_COLECTARE_DOLEANTE, grafic.getStare());
	}

	@Test
	void nuSePoateGeneraInainteDeTermen() {
		assertThrows(DomainException.class, () -> grafic.incepeAjustarea(LocalDate.of(2026, 10, 15)));
	}

	@Test
	void turaInAfaraLuniiEsteRespinsa() {
		assertThrows(DomainException.class, () -> grafic.adaugaTura(999, LocalDate.of(2026, 12, 1), TipTura.ZI));
	}

	@Test
	void turaSuprapusaEsteRespinsa_A2() {
		grafic.incepeAjustarea(LocalDate.of(2026, 10, 21));
		grafic.asigneaza(101, POPESCU, GradMedical.MEDIC_PRIMAR);       // 05.11 08-20
		grafic.adaugaTura(104, LocalDate.of(2026, 11, 5), TipTura.GARDA);  // 05.11 08 -> 06.11 08
		assertThrows(DomainException.class, () -> grafic.asigneaza(104, POPESCU, GradMedical.MEDIC_PRIMAR));
	}

	@Test
	void pauzaInsuficientaEsteRespinsa_A3() {
		grafic.incepeAjustarea(LocalDate.of(2026, 10, 21));
		grafic.asigneaza(102, POPESCU, GradMedical.MEDIC_PRIMAR);  // noapte 05.11 20 -> 06.11 08
		// tura de zi 06.11 incepe la 08: 0 h pauza
		assertThrows(DomainException.class, () -> grafic.asigneaza(103, POPESCU, GradMedical.MEDIC_PRIMAR));
	}

	@Test
	void publicareBlocataCandAcoperireaLipseste_A1() {
		grafic.incepeAjustarea(LocalDate.of(2026, 10, 21));
		grafic.asigneaza(101, POPESCU, GradMedical.MEDIC_PRIMAR);
		assertFalse(grafic.verificaRestrictii().isEmpty());
		assertThrows(DomainException.class, () -> grafic.publica(PUBLICARE));
		assertEquals(StareGrafic.IN_AJUSTARE, grafic.getStare());
	}

	@Test
	void cicluCompletPublicareConfirmareFinalizare() {
		graficPublicat();
		assertEquals(StareGrafic.PUBLICAT, grafic.getStare());
		Asignare a = grafic.gasesteTura(101).getAsignari().get(0);
		assertEquals(StareAsignare.IN_ASTEPTARE_CONFIRMARE, a.getStare());
		assertEquals(PUBLICARE.plusDays(3), a.getTermenConfirmare());

		grafic.confirmaAsignare(101, POPESCU);
		assertEquals(StareAsignare.CONFIRMATA, a.getStare());
		// celelalte doua se confirma tacit dupa 3 zile (WF5)
		assertEquals(2, grafic.aplicaConfirmariTacite(PUBLICARE.plusDays(4)));

		grafic.finalizeaza(LocalDate.of(2026, 12, 1));
		assertEquals(StareGrafic.FINALIZAT, grafic.getStare());
		assertTrue(grafic.getTure().stream().allMatch(t -> t.getStare() == StareTura.EFECTUATA));
	}

	@Test
	void concediulLasaTuraDescoperitaApoiRealocare() {
		graficPublicat();
		List<Tura> afectate = grafic.marcheazaDescoperita(POPESCU, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5));
		assertEquals(1, afectate.size());
		assertEquals(StareTura.DESCOPERITA, grafic.gasesteTura(101).getStare());
		assertEquals(1, grafic.tureDescoperite().size());

		grafic.realoca(101, MARIN, GradMedical.MEDIC_PRIMAR, PUBLICARE.plusDays(1));
		assertEquals(StareTura.PLANIFICATA, grafic.gasesteTura(101).getStare());
		assertTrue(grafic.tureDescoperite().isEmpty());
	}

	@Test
	void schimbDeTuraValidEsteAtomic() {
		graficPublicat();
		grafic.schimbaAsignari(101, POPESCU, 103, MARIN, PUBLICARE);
		assertTrue(grafic.gasesteTura(101).areAngajat(MARIN));
		assertTrue(grafic.gasesteTura(103).areAngajat(POPESCU));
		assertFalse(grafic.gasesteTura(101).areAngajat(POPESCU));
	}

	@Test
	void schimbDeTuraInvalidNuModificaNimic_A3() {
		grafic.incepeAjustarea(LocalDate.of(2026, 10, 21));
		grafic.adaugaTura(105, LocalDate.of(2026, 11, 6), TipTura.NOAPTE);   // 06.11 20 -> 07.11 08
		grafic.asigneaza(101, POPESCU, GradMedical.MEDIC_PRIMAR);            // zi 05.11
		grafic.asigneaza(102, IONESCU, GradMedical.MEDIC_PRIMAR);            // noapte 05.11
		grafic.asigneaza(103, MARIN, GradMedical.MEDIC_PRIMAR);              // zi 06.11
		grafic.asigneaza(105, POPESCU, GradMedical.MEDIC_PRIMAR);            // noapte 06.11
		grafic.publica(PUBLICARE);
		// Popescu ar lua ziua 06.11 (08-20) exact inaintea noptii lui din 06.11 (20-08) => 0 h pauza -> respins
		assertThrows(DomainException.class, () -> grafic.schimbaAsignari(101, POPESCU, 103, MARIN, PUBLICARE));
		// atomicitate: nimic nu s-a modificat
		assertTrue(grafic.gasesteTura(101).areAngajat(POPESCU));
		assertTrue(grafic.gasesteTura(103).areAngajat(MARIN));
	}

	@Test
	void valueObjectsValideaza() {
		assertThrows(DomainException.class, () -> new LunaCalendaristica(2026, 13));
		assertThrows(DomainException.class, () -> new CerintaAcoperire(TipTura.NOAPTE, GradMedical.MEDIC_PRIMAR, 0));
		assertEquals(30, NOIEMBRIE.zile().size());
		assertEquals(24.0, TipTura.GARDA.intervalPentru(LocalDate.of(2026, 11, 1)).durataOre());
	}

	private void graficPublicat() {
		grafic.incepeAjustarea(LocalDate.of(2026, 10, 21));
		grafic.asigneaza(101, POPESCU, GradMedical.MEDIC_PRIMAR);
		grafic.asigneaza(102, IONESCU, GradMedical.MEDIC_PRIMAR);
		grafic.asigneaza(103, MARIN, GradMedical.MEDIC_PRIMAR);
		grafic.publica(PUBLICARE);
	}
}
