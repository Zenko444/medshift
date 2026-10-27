package org.medshift.services.planificare;

import jakarta.annotation.PostConstruct;
import org.medshift.domain.planificare.CerintaAcoperire;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.MatriceAcoperire;
import org.medshift.domain.shared.GradMedical;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

// Aggregate Entity Factory pentru GraficLunar (livrabil Sprint 2).
@Component
public class GraficLunarFactoryImpl implements IGraficLunarFactory {
	private static final Logger logger = Logger.getLogger(GraficLunarFactoryImpl.class.getName());

	public static final Integer SECTIE_ATI = 1;

	private final IGraficLunarRepository repository;
	private final Clock clock;

	public GraficLunarFactoryImpl(IGraficLunarRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
		logger.info(">>> BEAN: GraficLunarFactoryImpl instantiated!");
	}

	@Override
	public GraficLunar buildGraficGol(Integer idSectie, LunaCalendaristica luna, LocalDate termenDoleante) {
		return new GraficLunar(repository.getNextID(), idSectie, luna, termenDoleante);
	}

	@Override
	public GraficLunar buildGraficCuTure(Integer idSectie, LunaCalendaristica luna, LocalDate termenDoleante,
			Set<TipTura> tipuriTure, MatriceAcoperire matrice) {
		GraficLunar grafic = buildGraficGol(idSectie, luna, termenDoleante);
		grafic.configureazaNecesar(matrice);
		int nr = 1;
		for (LocalDate zi : luna.zile())
			for (TipTura tip : TipTura.values())          // ordinea din enum: ZI, NOAPTE, GARDA
				if (tipuriTure.contains(tip))
					grafic.adaugaTura(grafic.getIdGrafic() * 1000 + nr++, zi, tip);
		return grafic;
	}

	/** Matricea standard folosita in exemple: 1 medic primar + 1 asistent pe zi, 1 + 1 pe noapte. */
	public static MatriceAcoperire matriceStandard() {
		return new MatriceAcoperire(List.of(
				new CerintaAcoperire(TipTura.ZI, GradMedical.MEDIC_PRIMAR, 1),
				new CerintaAcoperire(TipTura.ZI, GradMedical.ASISTENT_MEDICAL, 1),
				new CerintaAcoperire(TipTura.NOAPTE, GradMedical.MEDIC_PRIMAR, 1),
				new CerintaAcoperire(TipTura.NOAPTE, GradMedical.ASISTENT_MEDICAL, 1)));
	}

	@Override
	public IGraficLunarRepository getGraficLunarRepository() {
		return repository;
	}

	// Date de test: graficul sectiei ATI pentru luna urmatoare (ca initDomainServiceEntities din T2.C3)
	@PostConstruct
	public void initGraficeDemo() {
		LocalDate azi = LocalDate.now(clock);
		LunaCalendaristica lunaUrmatoare = LunaCalendaristica.din(azi.plusMonths(1));
		LocalDate termen = lunaUrmatoare.zile().get(0).minusDays(10);
		GraficLunar g = buildGraficCuTure(SECTIE_ATI, lunaUrmatoare, termen,
				Set.of(TipTura.ZI, TipTura.NOAPTE), matriceStandard());
		repository.add(g);
		logger.info(">> PostConstruct :: grafic demo " + g);
	}
}
