package org.medshift.application.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.MatriceAcoperire;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.StareGrafic;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;
import org.medshift.services.audit.IAuditService;
import org.medshift.services.planificare.IGraficLunarFactory;
import org.medshift.services.planificare.IGraficLunarRepository;
import org.medshift.services.planificare.IValidareGraficService;
import org.medshift.services.planificare.StrategieOptimizare;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Stratul Application: orchestreaza un use case (incarca agregatul, apeleaza domeniul, valideaza, auditeaza).
 * Regulile de business raman in GraficLunar; aici nu se ia nicio decizie de business.
 */
@Service
public class PlanificareWorkflowServiceImpl implements IPlanificareWorkflowService {
	private static final Logger logger = Logger.getLogger(PlanificareWorkflowServiceImpl.class.getName());

	private final IGraficLunarRepository repository;
	private final IGraficLunarFactory factory;
	private final IValidareGraficService validare;
	private final IAuditService audit;
	private final Map<String, StrategieOptimizare> strategii;   // Spring injecteaza TOATE strategiile, cheie = numele bean-ului
	private final Clock clock;

	public PlanificareWorkflowServiceImpl(IGraficLunarRepository repository, IGraficLunarFactory factory,
			IValidareGraficService validare, IAuditService audit, Map<String, StrategieOptimizare> strategii, Clock clock) {
		this.repository = repository;
		this.factory = factory;
		this.validare = validare;
		this.audit = audit;
		this.strategii = strategii;
		this.clock = clock;
		logger.info(">>> BEAN: PlanificareWorkflowServiceImpl instantiated with strategies " + strategii.keySet());
	}

	@Override
	public GraficLunar initializeazaGrafic(String utilizator, Integer idSectie, LunaCalendaristica luna,
			LocalDate termenDoleante, MatriceAcoperire matrice) {
		if (luna.compareTo(LunaCalendaristica.din(LocalDate.now(clock))) <= 0)
			throw new DomainException("Graficul se initializeaza doar pentru o luna viitoare.");   // V7
		GraficLunar grafic = factory.buildGraficCuTure(idSectie, luna, termenDoleante,
				Set.of(TipTura.ZI, TipTura.NOAPTE), matrice);
		List<String> erori = validare.valideazaCampuri(grafic);
		if (!erori.isEmpty())
			throw new DomainException("Grafic invalid: " + erori);
		repository.add(grafic);
		audit.inregistreaza(utilizator, "INITIALIZARE", cheie(grafic), "luna " + luna + ", " + grafic.getTure().size() + " ture");
		return grafic;
	}

	@Override
	public List<String> genereazaGrafic(String utilizator, Integer idGrafic, String strategie,
			List<PersonalDisponibil> personal) {
		StrategieOptimizare s = strategii.get(strategie);
		if (s == null)
			throw new DomainException("Strategie necunoscuta: " + strategie + ". Disponibile: " + strategii.keySet());
		GraficLunar grafic = repository.getById(idGrafic);
		if (grafic.getStare() == StareGrafic.IN_COLECTARE_DOLEANTE)
			grafic.incepeAjustarea(LocalDate.now(clock));
		s.genereaza(grafic, personal);
		List<String> probleme = validare.valideaza(grafic);
		audit.inregistreaza(utilizator, "GENERARE", cheie(grafic), "strategie=" + strategie + ", probleme=" + probleme.size());
		return probleme;
	}

	@Override
	public Set<String> strategiiDisponibile() {
		return new TreeSet<>(strategii.keySet());
	}

	@Override
	public void publicaGrafic(String utilizator, Integer idGrafic) {
		GraficLunar grafic = repository.getById(idGrafic);
		List<String> probleme = validare.valideaza(grafic);
		if (!probleme.isEmpty())
			throw new DomainException("Graficul nu poate fi publicat: " + probleme);
		grafic.publica(LocalDateTime.now(clock));
		audit.inregistreaza(utilizator, "PUBLICARE", cheie(grafic), "termen confirmare: 3 zile");
		// TODO (Sprint urmator): notificarea personalului prin serviciul de notificari
	}

	@Override
	public void confirmaTura(String utilizator, Integer idGrafic, Integer idTura, Integer idAngajat) {
		GraficLunar grafic = repository.getById(idGrafic);
		grafic.confirmaAsignare(idTura, idAngajat);
		audit.inregistreaza(utilizator, "CONFIRMARE", cheie(grafic), "tura " + idTura + ", angajat " + idAngajat);
	}

	@Override
	public int aplicaConfirmariTacite(Integer idGrafic) {
		GraficLunar grafic = repository.getById(idGrafic);
		int n = grafic.aplicaConfirmariTacite(LocalDateTime.now(clock));
		if (n > 0)
			audit.inregistreaza("SISTEM", "CONFIRMARE_TACITA", cheie(grafic), n + " asignari");
		return n;
	}

	@Override
	public List<Tura> inregistreazaConcediu(String utilizator, Integer idGrafic, Integer idAngajat, LocalDate deLa,
			LocalDate panaLa) {
		GraficLunar grafic = repository.getById(idGrafic);
		List<Tura> afectate = grafic.marcheazaDescoperita(idAngajat, deLa, panaLa);
		audit.inregistreaza(utilizator, "CONCEDIU", cheie(grafic),
				"angajat " + idAngajat + " " + deLa + ".." + panaLa + ", ture afectate: " + afectate.size());
		return afectate;
	}

	@Override
	public void schimbaTure(String utilizator, Integer idGrafic, Integer idTuraA, Integer idAngajatA, Integer idTuraB,
			Integer idAngajatB) {
		GraficLunar grafic = repository.getById(idGrafic);
		grafic.schimbaAsignari(idTuraA, idAngajatA, idTuraB, idAngajatB, LocalDateTime.now(clock));
		audit.inregistreaza(utilizator, "SCHIMB_TURA", cheie(grafic),
				idAngajatA + "@" + idTuraA + " <-> " + idAngajatB + "@" + idTuraB);
	}

	@Override
	public void finalizeazaGrafic(String utilizator, Integer idGrafic) {
		GraficLunar grafic = repository.getById(idGrafic);
		grafic.finalizeaza(LocalDate.now(clock));
		audit.inregistreaza(utilizator, "FINALIZARE", cheie(grafic), "ture efectuate: " + grafic.getTure().size());
		// TODO (contextul Pontaj): evenimentul GraficFinalizat porneste generarea fiselor de pontaj
	}

	private static String cheie(GraficLunar g) {
		return "GraficLunar#" + g.getIdGrafic();
	}
}
