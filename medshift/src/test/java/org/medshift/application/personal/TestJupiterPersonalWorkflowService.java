package org.medshift.application.personal;

import org.junit.jupiter.api.Test;
import org.medshift.application.planificare.IPlanificareWorkflowService;
import org.medshift.domain.personal.*;
import org.medshift.domain.planificare.*;
import org.medshift.domain.shared.*;
import org.medshift.services.planificare.GraficLunarFactoryImpl;
import org.medshift.support.CeasControlabil;
import org.medshift.support.TestClockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// JUnit 5: test pentru Workflow Service din contextul Personal, impreuna cu Planificarea
@SpringBootTest
@Import(TestClockConfig.class)
public class TestJupiterPersonalWorkflowService {
	private static final Logger logger = Logger.getLogger(TestJupiterPersonalWorkflowService.class.getName());

	@Autowired private IPersonalWorkflowService personal;
	@Autowired private IPlanificareWorkflowService planificare;
	@Autowired private CeasControlabil ceas;

	private static final LunaCalendaristica DECEMBRIE = new LunaCalendaristica(2026, 12);
	private static final LocalDate TERMEN = LocalDate.of(2026, 11, 20);

	private List<Angajat> angajeazaEchipa(int sectie) {
		List<Angajat> echipa = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			echipa.add(personal.inregistreazaAngajat("hr", "Medic" + i, "S" + sectie, "m" + i + ".s" + sectie + "@test.ro",
					GradMedical.MEDIC_PRIMAR, "ATI", sectie, Contract.normaIntreaga(LocalDate.of(2022, 1, 1), true)));
			echipa.add(personal.inregistreazaAngajat("hr", "Asistent" + i, "S" + sectie, "a" + i + ".s" + sectie + "@test.ro",
					GradMedical.ASISTENT_MEDICAL, "ATI", sectie, Contract.normaIntreaga(LocalDate.of(2022, 1, 1), true)));
		}
		return echipa;
	}

	/** Pregateste: echipa, graficul pe decembrie, doleante pentru primul medic, generare si publicare. */
	private GraficLunar graficPublicat(int sectie, Angajat cuDoleante, Set<LocalDate> preferaLiber, LocalDate indisponibil) {
		ceas.seteaza(TestClockConfig.START);
		GraficLunar g = planificare.initializeazaGrafic("sef.sectie", sectie, DECEMBRIE, TERMEN, GraficLunarFactoryImpl.matriceStandard());
		for (LocalDate zi : preferaLiber)
			personal.depuneDoleanta("medic", cuDoleante.getIdAngajat(), DECEMBRIE,
					new PreferintaZi(zi, TipPreferinta.PREFER_LIBER, null));
		personal.depuneDoleanta("medic", cuDoleante.getIdAngajat(), DECEMBRIE, new PreferintaZi(indisponibil, TipPreferinta.INDISPONIBIL, null));
		ceas.seteaza(LocalDateTime.of(2026, 11, 21, 10, 0));      // termenul de doleante a trecut
		List<PersonalDisponibil> disponibili = personal.pregatesteDisponibilitati(sectie, DECEMBRIE);
		assertTrue(planificare.genereazaGrafic("sef.sectie", g.getIdGrafic(), "prioritateDoleante", disponibili).isEmpty());
		planificare.publicaGrafic("sef.sectie", g.getIdGrafic());
		return g;
	}

	@Test
	public void doleanteleAjungInGeneraresiSuntRespectate() {
		List<Angajat> echipa = angajeazaEchipa(60);
		Angajat m1 = echipa.get(0);
		Set<LocalDate> liber = Set.of(LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 25));
		LocalDate indisponibil = LocalDate.of(2026, 12, 31);
		GraficLunar g = graficPublicat(60, m1, liber, indisponibil);

		assertTrue(g.tureAngajat(m1.getIdAngajat()).stream()
				.noneMatch(t -> liber.contains(t.getData()) || t.getData().equals(indisponibil)));
		// dupa termen nu se mai pot depune doleante
		assertThrows(DomainException.class, () -> personal.depuneDoleanta("medic", m1.getIdAngajat(), DECEMBRIE,
				new PreferintaZi(LocalDate.of(2026, 12, 3), TipPreferinta.PREFER_LIBER, null)));
	}

	@Test
	public void concediulAprobatElibereazaTureleDinGraficulPublicat() {
		List<Angajat> echipa = angajeazaEchipa(61);
		GraficLunar g = graficPublicat(61, echipa.get(0), Set.of(), LocalDate.of(2026, 12, 31));
		Angajat m2 = echipa.get(2);
		Perioada perioada = new Perioada(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 18));

		Concediu c = personal.solicitaConcediu("medic", m2.getIdAngajat(), TipConcediu.ODIHNA, perioada);
		assertEquals(StareConcediu.SOLICITAT, c.getStare());
		assertTrue(g.tureAngajat(m2.getIdAngajat()).stream().anyMatch(t -> perioada.contine(t.getData())),
				"cat e doar solicitat, turele raman");

		List<Tura> afectate = personal.aprobaConcediu("hr", c.getIdConcediu());
		logger.info("Ture eliberate de concediu: " + afectate.size());
		assertFalse(afectate.isEmpty());
		assertTrue(g.tureAngajat(m2.getIdAngajat()).stream().noneMatch(t -> perioada.contine(t.getData())));
		assertFalse(g.tureDescoperite().isEmpty(), "seful de sectie vede turele de realocat");

		// urgenta medicala: aprobata automat, efect imediat
		Angajat m3 = echipa.get(4);
		Perioada urgenta = new Perioada(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 8));
		personal.solicitaConcediu("medic", m3.getIdAngajat(), TipConcediu.URGENTA_MEDICALA, urgenta);
		assertTrue(g.tureAngajat(m3.getIdAngajat()).stream().noneMatch(t -> urgenta.contine(t.getData())));
	}

	@Test
	public void schimbDeTuraIntreMediciAprobatAutomatSauRefuzat() {
		List<Angajat> echipa = angajeazaEchipa(62);
		GraficLunar g = graficPublicat(62, echipa.get(0), Set.of(), LocalDate.of(2026, 12, 31));
		LocalDateTime acum = LocalDateTime.of(2026, 11, 21, 10, 0);
		List<Integer> medici = echipa.stream().filter(a -> a.getGrad() == GradMedical.MEDIC_PRIMAR).map(Angajat::getIdAngajat).toList();

		// cautam doua ture ale unor medici diferiti pe care regulile permit sa le schimbe
		int[] pereche = gasestePerecheValida(g, medici, acum);
		assertNotNull(pereche, "exista macar un schimb valid intre medici");
		CerereSchimbTura c = personal.cereSchimbTura("medic", g.getIdGrafic(), pereche[0], pereche[1], pereche[2], pereche[3]);
		personal.raspundeColegul("medic", c.getIdCerere(), true, null);
		assertEquals(StareSchimb.APROBATA, c.getStare(), "acelasi grad => aprobare automata");
		assertTrue(g.gasesteTura(pereche[1]).areAngajat(pereche[2]));
		assertTrue(g.gasesteTura(pereche[3]).areAngajat(pereche[0]));

		// refuzul colegului
		int[] alta = gasestePerecheValida(g, medici, acum);
		CerereSchimbTura refuzata = personal.cereSchimbTura("medic", g.getIdGrafic(), alta[0], alta[1], alta[2], alta[3]);
		personal.raspundeColegul("medic", refuzata.getIdCerere(), false, "Am alt program");
		assertEquals(StareSchimb.RESPINSA, refuzata.getStare());

		// medic <-> asistent: tura ar ramane fara medic primar (A1), deci cererea nu e acceptata
		Integer asistent = echipa.get(1).getIdAngajat();
		Integer turaMedic = g.tureAngajat(medici.get(0)).get(0).getIdTura();
		Integer turaAsistent = g.tureAngajat(asistent).stream().filter(t -> !t.getIdTura().equals(turaMedic)).findFirst().orElseThrow().getIdTura();
		assertThrows(DomainException.class, () -> personal.cereSchimbTura("medic", g.getIdGrafic(),
				medici.get(0), turaMedic, asistent, turaAsistent));
	}

	/** Intoarce {idSolicitant, idTuraOferita, idColeg, idTuraSolicitata} pentru primul schimb valid gasit. */
	private static int[] gasestePerecheValida(GraficLunar g, List<Integer> angajati, LocalDateTime acum) {
		for (Integer a : angajati)
			for (Integer b : angajati)
				if (!a.equals(b))
					for (Tura ta : g.tureAngajat(a))
						for (Tura tb : g.tureAngajat(b))
							if (g.verificaSchimb(ta.getIdTura(), a, tb.getIdTura(), b, acum).isEmpty())
								return new int[] {a, ta.getIdTura(), b, tb.getIdTura()};
		return null;
	}
}
