package org.medshift.services.pontaj;

import org.medshift.domain.planificare.Asignare;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.StareGrafic;
import org.medshift.domain.planificare.StareTura;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.springframework.stereotype.Component;

import java.util.*;

// Aggregate Entity Factory pentru FisaPontaj (livrabil Sprint 2).
@Component
public class FisaPontajFactoryImpl implements IFisaPontajFactory {

	private final IFisaPontajRepository repository;
	private final ISegmentareOreService segmentare;

	public FisaPontajFactoryImpl(IFisaPontajRepository repository, ISegmentareOreService segmentare) {
		this.repository = repository;
		this.segmentare = segmentare;
	}

	@Override
	public FisaPontaj buildFisaGoala(Integer idAngajat, Integer idSectie, LunaCalendaristica luna) {
		return new FisaPontaj(repository.getNextID(), idAngajat, idSectie, luna);
	}

	@Override
	public List<FisaPontaj> buildFiseDinGrafic(GraficLunar g) {
		if (g.getStare() != StareGrafic.FINALIZAT)
			throw new DomainException("Fisele de pontaj se genereaza doar dintr-un grafic finalizat (WF8).");
		Map<Integer, FisaPontaj> fise = new TreeMap<>();
		List<Tura> ture = g.getTure().stream()
				.filter(t -> t.getStare() == StareTura.EFECTUATA)
				.sorted(Comparator.comparing(t -> t.getInterval().inceput())).toList();
		for (Tura t : ture)
			for (Asignare a : t.asignariActive()) {
				FisaPontaj fisa = fise.computeIfAbsent(a.getIdAngajat(),
						id -> buildFisaGoala(id, g.getIdSectie(), g.getLuna()));
				fisa.adaugaLinie(t.getIdTura(), t.getData(), t.getTip(), t.getInterval(),
						segmentare.segmenteaza(t.getInterval(), t.getTip()));
			}
		return new ArrayList<>(fise.values());
	}
}
