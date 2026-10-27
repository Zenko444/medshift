package org.medshift.domain.pontaj;

import org.junit.jupiter.api.Test;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: regulile entitatilor din contextul Pontaj (fara Spring)
public class TestJupiterEntitatiPontaj {

	static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);

	private FisaPontaj fisaCuOGarda() {
		FisaPontaj f = new FisaPontaj(1, 101, 1, NOIEMBRIE);
		LocalDate zi = LocalDate.of(2026, 11, 7);   // sambata
		f.adaugaLinie(7001, zi, TipTura.GARDA, TipTura.GARDA.intervalPentru(zi),
				List.of(new SegmentOre(CategorieOre.GARDA, 16), new SegmentOre(CategorieOre.NOAPTE, 8)));
		return f;
	}

	@Test
	void totaluriSiOreSuplimentare() {
		FisaPontaj f = fisaCuOGarda();
		TotalOre t = f.totaluri();
		assertEquals(16.0, t.oreGarda());
		assertEquals(8.0, t.oreNoapte());
		assertEquals(24.0, t.total());
		assertEquals(0.0, t.oreSuplimentare(168));
		assertEquals(4.0, new TotalOre(100, 40, 20, 12, 0).oreSuplimentare(168));
	}

	@Test
	void segmenteleTrebuieSaAcopereExactTura() {
		FisaPontaj f = new FisaPontaj(2, 102, 1, NOIEMBRIE);
		LocalDate zi = LocalDate.of(2026, 11, 5);
		assertThrows(DomainException.class, () -> f.adaugaLinie(7002, zi, TipTura.ZI, TipTura.ZI.intervalPentru(zi),
				List.of(new SegmentOre(CategorieOre.ZI, 10))), "10 h declarate pentru o tura de 12 h");
		assertThrows(DomainException.class, () -> new SegmentOre(CategorieOre.ZI, -1));
	}

	@Test
	void fluxFisa_verificareAprobareExport() {
		FisaPontaj f = fisaCuOGarda();
		assertThrows(DomainException.class, f::aprobaHR, "HR nu aproba o fisa neverificata (A10)");
		f.inregistreazaVerificare(List.of(new Neconformitate("A3", "pauza sub 24 h dupa garda", LocalDate.of(2026, 11, 8))));
		assertEquals(StareFisa.NECONFORMA, f.getStare());
		f.inregistreazaVerificare(List.of());
		assertEquals(StareFisa.VALIDATA_LEGAL, f.getStare());
		f.aprobaHR();

		BorderouSalarial b = new BorderouSalarial(1, 1, NOIEMBRIE, FormatExport.CSV);
		b.adaugaFisa(f);
		assertThrows(DomainException.class, () -> b.adaugaFisa(f), "un angajat apare o singura data");
		b.marcheazaExportat(List.of(f));
		assertEquals(StareBorderou.EXPORTAT, b.getStare());
		assertEquals(StareFisa.EXPORTATA, f.getStare());
		assertThrows(DomainException.class, () -> f.adaugaLinie(7003, LocalDate.of(2026, 11, 9), TipTura.ZI,
				TipTura.ZI.intervalPentru(LocalDate.of(2026, 11, 9)), List.of(new SegmentOre(CategorieOre.ZI, 12))),
				"o fisa exportata nu se mai modifica");
	}

	@Test
	void borderoulRefuzaFiseNeaprobateSauDinAltaLuna() {
		BorderouSalarial b = new BorderouSalarial(2, 1, NOIEMBRIE, FormatExport.CSV);
		assertThrows(DomainException.class, () -> b.adaugaFisa(fisaCuOGarda()), "fisa nu e aprobata HR");
		assertThrows(DomainException.class, () -> b.marcheazaExportat(List.of()), "borderou gol");
	}
}
