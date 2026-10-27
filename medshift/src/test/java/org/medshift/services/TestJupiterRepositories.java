package org.medshift.services;

import org.junit.jupiter.api.Test;
import org.medshift.domain.personal.*;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.pontaj.*;
import org.medshift.domain.shared.*;
import org.medshift.services.personal.*;
import org.medshift.services.planificare.IGraficLunarRepository;
import org.medshift.services.pontaj.IBorderouSalarialRepository;
import org.medshift.services.pontaj.IFisaPontajRepository;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: cate un test pentru repository-ul fiecarui Aggregate Root
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterRepositories {
	private static final Logger logger = Logger.getLogger(TestJupiterRepositories.class.getName());

	@Autowired private IGraficLunarRepository grafice;
	@Autowired private IAngajatRepository angajati;
	@Autowired private IDoleanteLunareRepository doleante;
	@Autowired private IConcediuRepository concedii;
	@Autowired private ICerereSchimbTuraRepository cereri;
	@Autowired private IFisaPontajRepository fise;
	@Autowired private IBorderouSalarialRepository borderouri;

	private static final LunaCalendaristica NOIEMBRIE = new LunaCalendaristica(2026, 11);

	private Angajat angajatNou(String email, GradMedical grad, int sectie) {
		return new Angajat(null, new NumeComplet("Popescu", "Ana"), new Email(email), grad, "ATI", sectie,
				Contract.normaIntreaga(LocalDate.of(2020, 1, 1), true));
	}

	@Test
	public void graficLunar_queryByExampleSiCautarePeSectie() {
		GraficLunar g = new GraficLunar(grafice.getNextID(), 501, NOIEMBRIE, LocalDate.of(2026, 10, 20));
		grafice.add(g);
		GraficLunar exemplu = new GraficLunar(g.getIdGrafic(), 501, NOIEMBRIE, LocalDate.of(2026, 10, 20));
		assertSame(g, grafice.get(exemplu), "query by example intoarce obiectul stocat cu acelasi ID");
		assertEquals(1, grafice.findBySectie(501).size());
		assertTrue(grafice.getBySectieSiLuna(501, NOIEMBRIE).isPresent());
	}

	@Test
	public void angajat_idGeneratEmailUnicSiCautari() {
		Angajat a = angajati.add(angajatNou("ana.popescu@arcadia.ro", GradMedical.MEDIC_PRIMAR, 601));
		assertNotNull(a.getIdAngajat(), "repository-ul a dat un ID nou");
		assertSame(a, angajati.getById(a.getIdAngajat()));
		assertTrue(angajati.findByEmail(new Email("ANA.POPESCU@arcadia.ro")).isPresent(), "emailul se compara fara majuscule");
		assertThrows(DomainException.class, () -> angajati.add(angajatNou("ana.popescu@arcadia.ro", GradMedical.ASISTENT_MEDICAL, 601)));

		angajati.add(angajatNou("ion.ionescu@arcadia.ro", GradMedical.ASISTENT_MEDICAL, 601));
		assertEquals(2, angajati.findBySectie(601).size());
		assertEquals(1, angajati.findBySectieSiGrad(601, GradMedical.MEDIC_PRIMAR).size());
		assertThrows(DomainException.class, () -> angajati.getById(99999));
		assertTrue(angajati.findById(99999).isEmpty());
	}

	@Test
	public void doleante_oSinguraColectiePeAngajatSiLuna() {
		DoleanteLunare d = doleante.add(new DoleanteLunare(null, 701, NOIEMBRIE));
		d.adaugaPreferinta(new PreferintaZi(LocalDate.of(2026, 11, 7), TipPreferinta.PREFER_LIBER, null),
				LocalDate.of(2026, 10, 20), LocalDateTime.of(2026, 10, 15, 9, 0));
		assertSame(d, doleante.getByAngajatSiLuna(701, NOIEMBRIE).orElseThrow());
		assertThrows(DomainException.class, () -> doleante.add(new DoleanteLunare(null, 701, NOIEMBRIE)));
	}

	@Test
	public void concediu_suprapunereRespinsaSiCautareAprobate() {
		Concediu c = new Concediu(null, 801, TipConcediu.ODIHNA, new Perioada(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14)));
		concedii.add(c);
		assertThrows(DomainException.class, () -> concedii.add(new Concediu(null, 801, TipConcediu.MEDICAL,
				new Perioada(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 20)))), "regula A9");
		Perioada noiembrie = new Perioada(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
		assertTrue(concedii.findAprobateInPerioada(noiembrie).stream().noneMatch(x -> x.getIdAngajat().equals(801)),
				"un concediu doar solicitat nu blocheaza turele");
		c.aproba();
		assertTrue(concedii.findAprobateInPerioada(noiembrie).contains(c));
	}

	@Test
	public void cerereSchimb_cereriDeschisePeAngajat() {
		CerereSchimbTura c = cereri.add(new CerereSchimbTura(null, 1, 901, 902, 1001, 1003));
		assertEquals(List.of(c), cereri.findDeschiseByAngajat(902));
		c.acceptaDeColeg();
		c.aproba();
		assertTrue(cereri.findDeschiseByAngajat(902).isEmpty(), "o cerere aprobata nu mai e deschisa");
	}

	@Test
	public void fisaSiBorderou_unicitateSiTotaluri() {
		FisaPontaj f = fise.add(new FisaPontaj(null, 1101, 1201, NOIEMBRIE));
		LocalDate zi = LocalDate.of(2026, 11, 5);
		f.adaugaLinie(5001, zi, TipTura.NOAPTE, TipTura.NOAPTE.intervalPentru(zi),
				List.of(new SegmentOre(CategorieOre.ZI, 2), new SegmentOre(CategorieOre.NOAPTE, 10)));
		assertEquals(12.0, f.totaluri().total());
		assertThrows(DomainException.class, () -> fise.add(new FisaPontaj(null, 1101, 1201, NOIEMBRIE)));

		f.inregistreazaVerificare(List.of());
		f.aprobaHR();
		assertEquals(1, fise.findByLunaSiStare(NOIEMBRIE, StareFisa.APROBATA_HR).stream()
				.filter(x -> x.getIdAngajat().equals(1101)).count());

		BorderouSalarial b = borderouri.add(new BorderouSalarial(null, 1201, NOIEMBRIE, FormatExport.CSV));
		b.adaugaFisa(f);
		b.marcheazaExportat(List.of(f));
		assertEquals(StareFisa.EXPORTATA, f.getStare());
		assertThrows(DomainException.class, () -> borderouri.add(new BorderouSalarial(null, 1201, NOIEMBRIE, FormatExport.XML)));
		logger.info("Borderou: " + b + ", total ore: " + b.totalSectie().total());
	}
}
