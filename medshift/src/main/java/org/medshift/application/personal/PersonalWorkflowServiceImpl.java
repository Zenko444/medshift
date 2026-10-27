package org.medshift.application.personal;

import org.medshift.application.planificare.IPlanificareWorkflowService;
import org.medshift.domain.personal.*;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.StareGrafic;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.GradMedical;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.Perioada;
import org.medshift.services.audit.IAuditService;
import org.medshift.services.personal.*;
import org.medshift.services.planificare.IGraficLunarRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Stratul Application pentru Personal. Coordoneaza si cu Planificarea:
 * un concediu aprobat si un schimb de tura aprobat se reflecta automat in graficul publicat.
 */
@Service
public class PersonalWorkflowServiceImpl implements IPersonalWorkflowService {
	private static final Logger logger = Logger.getLogger(PersonalWorkflowServiceImpl.class.getName());

	private final IAngajatRepository angajati;
	private final IAngajatFactory factory;
	private final IDoleanteLunareRepository doleante;
	private final IConcediuRepository concedii;
	private final ICerereSchimbTuraRepository cereri;
	private final IValidarePersonalService validare;
	private final IDisponibilitateService disponibilitate;
	private final IGraficLunarRepository grafice;               // doar citire: termen de doleante, ture
	private final IPlanificareWorkflowService planificare;      // modificarile in grafic trec prin workflow-ul lui
	private final IAuditService audit;
	private final Clock clock;

	public PersonalWorkflowServiceImpl(IAngajatRepository angajati, IAngajatFactory factory,
			IDoleanteLunareRepository doleante, IConcediuRepository concedii, ICerereSchimbTuraRepository cereri,
			IValidarePersonalService validare, IDisponibilitateService disponibilitate, IGraficLunarRepository grafice,
			IPlanificareWorkflowService planificare, IAuditService audit, Clock clock) {
		this.angajati = angajati;
		this.factory = factory;
		this.doleante = doleante;
		this.concedii = concedii;
		this.cereri = cereri;
		this.validare = validare;
		this.disponibilitate = disponibilitate;
		this.grafice = grafice;
		this.planificare = planificare;
		this.audit = audit;
		this.clock = clock;
		logger.info(">>> BEAN: PersonalWorkflowServiceImpl instantiated!");
	}

	/* =================== UC2.1 =================== */

	@Override
	public Angajat inregistreazaAngajat(String utilizator, String nume, String prenume, String email, GradMedical grad,
			String specializare, Integer idSectie, Contract contract) {
		Angajat a = factory.buildAngajat(nume, prenume, email, grad, specializare, idSectie, contract);
		aruncaDacaExista(validare.valideazaAngajat(a), "Angajat invalid");
		angajati.add(a);
		audit.inregistreaza(utilizator, "ANGAJARE", "Angajat#" + a.getIdAngajat(), grad + ", sectia " + idSectie);
		return a;
	}

	/* =================== UC2.2 =================== */

	@Override
	public DoleanteLunare depuneDoleanta(String utilizator, Integer idAngajat, LunaCalendaristica luna,
			PreferintaZi preferinta) {
		Angajat a = angajati.getById(idAngajat);
		GraficLunar grafic = grafice.getBySectieSiLuna(a.getIdSectie(), luna).orElseThrow(() -> new DomainException(
				"Graficul sectiei " + a.getIdSectie() + " pentru luna " + luna + " nu a fost initializat."));
		if (grafic.getStare() != StareGrafic.IN_COLECTARE_DOLEANTE)
			throw new DomainException("Colectarea doleantelor pentru luna " + luna + " s-a incheiat.");
		LocalDateTime acum = LocalDateTime.now(clock);
		DoleanteLunare d = doleante.getByAngajatSiLuna(idAngajat, luna)
				.orElseGet(() -> new DoleanteLunare(doleante.getNextID(), idAngajat, luna));
		aruncaDacaExista(validare.valideazaDoleante(d, grafic.getTermenDoleante(), acum), "Doleanta respinsa");
		d.adaugaPreferinta(preferinta, grafic.getTermenDoleante(), acum);
		doleante.add(d);       // add pe un obiect existent il pastreaza (acelasi ID)
		audit.inregistreaza(utilizator, "DOLEANTA", "Angajat#" + idAngajat, preferinta.tip() + " " + preferinta.data());
		return d;
	}

	@Override
	public List<PersonalDisponibil> pregatesteDisponibilitati(Integer idSectie, LunaCalendaristica luna) {
		return disponibilitate.personalDisponibil(idSectie, luna);
	}

	/* =================== UC2.4 =================== */

	@Override
	public Concediu solicitaConcediu(String utilizator, Integer idAngajat, TipConcediu tip, Perioada perioada) {
		angajati.getById(idAngajat);
		Concediu c = new Concediu(concedii.getNextID(), idAngajat, tip, perioada);
		aruncaDacaExista(validare.valideazaConcediu(c), "Concediu invalid");
		concedii.add(c);
		audit.inregistreaza(utilizator, "CONCEDIU_SOLICITAT", "Concediu#" + c.getIdConcediu(), tip + " " + perioada);
		if (c.getStare() == StareConcediu.APROBAT)          // urgenta medicala: efect imediat in grafic (R2)
			actualizeazaGrafice(utilizator, c);
		return c;
	}

	@Override
	public List<Tura> aprobaConcediu(String utilizator, Integer idConcediu) {
		Concediu c = concedii.getById(idConcediu);
		c.aproba();
		audit.inregistreaza(utilizator, "CONCEDIU_APROBAT", "Concediu#" + idConcediu, c.getPerioada().toString());
		return actualizeazaGrafice(utilizator, c);
	}

	@Override
	public void respingeConcediu(String utilizator, Integer idConcediu, String motiv) {
		Concediu c = concedii.getById(idConcediu);
		c.respinge(motiv);
		audit.inregistreaza(utilizator, "CONCEDIU_RESPINS", "Concediu#" + idConcediu, motiv);
	}

	/** Concediul aprobat elibereaza turele din graficele deja publicate ale sectiei (CF2.4). */
	private List<Tura> actualizeazaGrafice(String utilizator, Concediu c) {
		Angajat a = angajati.getById(c.getIdAngajat());
		List<Tura> afectate = new ArrayList<>();
		LunaCalendaristica luna = LunaCalendaristica.din(c.getPerioada().inceput());
		LunaCalendaristica ultima = LunaCalendaristica.din(c.getPerioada().sfarsit());
		while (luna.compareTo(ultima) <= 0) {
			LunaCalendaristica l = luna;
			grafice.getBySectieSiLuna(a.getIdSectie(), l)
					.filter(g -> g.getStare() == StareGrafic.PUBLICAT)
					.ifPresent(g -> {
						LocalDate deLa = max(c.getPerioada().inceput(), l.zile().get(0));
						LocalDate panaLa = min(c.getPerioada().sfarsit(), l.ultimaZi());
						afectate.addAll(planificare.inregistreazaConcediu(utilizator, g.getIdGrafic(), a.getIdAngajat(), deLa, panaLa));
					});
			luna = LunaCalendaristica.din(l.ultimaZi().plusDays(1));
		}
		return afectate;
	}

	/* =================== UC2.3 =================== */

	@Override
	public CerereSchimbTura cereSchimbTura(String utilizator, Integer idGrafic, Integer idSolicitant,
			Integer idTuraOferita, Integer idColeg, Integer idTuraSolicitata) {
		GraficLunar g = grafice.getById(idGrafic);
		// Verificare eligibilitate coleg (<<include>> in UC2.3): pe ciorna, fara sa modifice graficul
		List<String> probleme = g.verificaSchimb(idTuraOferita, idSolicitant, idTuraSolicitata, idColeg,
				LocalDateTime.now(clock));
		aruncaDacaExista(probleme, "Schimbul nu este posibil");
		CerereSchimbTura c = cereri.add(new CerereSchimbTura(null, idGrafic, idSolicitant, idColeg, idTuraOferita, idTuraSolicitata));
		audit.inregistreaza(utilizator, "SCHIMB_CERUT", "CerereSchimbTura#" + c.getIdCerere(),
				idSolicitant + "@" + idTuraOferita + " <-> " + idColeg + "@" + idTuraSolicitata);
		// TODO (sprint urmator): notificare catre coleg
		return c;
	}

	@Override
	public CerereSchimbTura raspundeColegul(String utilizator, Integer idCerere, boolean accepta, String motiv) {
		CerereSchimbTura c = cereri.getById(idCerere);
		if (!accepta) {
			c.respinge(motiv);
			audit.inregistreaza(utilizator, "SCHIMB_REFUZAT", "CerereSchimbTura#" + idCerere, motiv);
			return c;
		}
		c.acceptaDeColeg();
		GradMedical gradSolicitant = angajati.getById(c.getIdSolicitant()).getGrad();
		GradMedical gradColeg = angajati.getById(c.getIdColeg()).getGrad();
		if (gradSolicitant == gradColeg) {
			aplicaSchimbul(utilizator, c);                    // aprobare automata
		} else {
			c.trimiteLaAprobare();                            // aprobare conditionata de seful de sectie
			audit.inregistreaza(utilizator, "SCHIMB_LA_APROBARE", "CerereSchimbTura#" + idCerere, "grade diferite");
		}
		return c;
	}

	@Override
	public CerereSchimbTura aprobaSchimbDeSef(String utilizator, Integer idCerere) {
		CerereSchimbTura c = cereri.getById(idCerere);
		if (c.getStare() != StareSchimb.IN_APROBARE)
			throw new DomainException("Cererea " + idCerere + " nu asteapta aprobarea sefului de sectie.");
		aplicaSchimbul(utilizator, c);
		return c;
	}

	/** Intai schimbul in grafic (poate esua), abia apoi cererea devine APROBATA: totul sau nimic. */
	private void aplicaSchimbul(String utilizator, CerereSchimbTura c) {
		planificare.schimbaTure(utilizator, c.getIdGrafic(), c.getIdTuraOferita(), c.getIdSolicitant(),
				c.getIdTuraSolicitata(), c.getIdColeg());
		c.aproba();
		audit.inregistreaza(utilizator, "SCHIMB_APROBAT", "CerereSchimbTura#" + c.getIdCerere(), "aplicat in grafic");
	}

	/* =================== utilitare =================== */

	private static void aruncaDacaExista(List<String> erori, String mesaj) {
		if (!erori.isEmpty())
			throw new DomainException(mesaj + ": " + erori);
	}

	private static LocalDate max(LocalDate a, LocalDate b) {
		return a.isAfter(b) ? a : b;
	}

	private static LocalDate min(LocalDate a, LocalDate b) {
		return a.isBefore(b) ? a : b;
	}
}
