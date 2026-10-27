package org.medshift.services.planificare;

import org.medshift.domain.planificare.Asignare;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.TipTura;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// Domain Computation Service: campuri si view-uri calculate pentru Planificare (regulile C1, C2 si 5.4 din Sprint 1).
@Service
public class CalculPlanificareServiceImpl implements ICalculPlanificareService {

	@Override
	public double gradAcoperire(GraficLunar grafic, Integer idTura) {
		return grafic.gasesteTura(idTura).gradAcoperire(grafic.getMatrice());
	}

	@Override
	public double orePlanificate(GraficLunar grafic, Integer idAngajat) {
		return grafic.tureAngajat(idAngajat).stream().mapToDouble(t -> t.getInterval().durataOre()).sum();
	}

	@Override
	public double gradAcoperireGrafic(GraficLunar grafic) {
		return grafic.getTure().stream().mapToDouble(t -> t.gradAcoperire(grafic.getMatrice())).average().orElse(1.0);
	}

	@Override
	public List<Tura> raportTureSubNecesar(GraficLunar grafic) {
		return grafic.tureDescoperite();
	}

	@Override
	public List<IndicatoriAngajat> raportEchitate(GraficLunar grafic) {
		return angajati(grafic).stream().map(id -> {
			List<Tura> ture = grafic.tureAngajat(id);
			return new IndicatoriAngajat(id, ture.size(),
					ture.stream().filter(t -> t.getTip() == TipTura.NOAPTE).count(),
					ture.stream().filter(t -> t.getTip() == TipTura.GARDA).count(),
					ture.stream().filter(CalculPlanificareServiceImpl::esteWeekend).count(),
					orePlanificate(grafic, id));
		}).sorted(Comparator.comparing(IndicatoriAngajat::idAngajat)).toList();
	}

	@Override
	public Map<Integer, Long> distributieGarziWeekend(GraficLunar grafic) {
		Map<Integer, Long> rezultat = new TreeMap<>();
		for (Integer id : angajati(grafic))
			rezultat.put(id, grafic.tureAngajat(id).stream()
					.filter(t -> t.getTip() != TipTura.ZI && esteWeekend(t)).count());
		return rezultat;
	}

	@Override
	public long diferentaMaximaTureGrele(GraficLunar grafic) {
		LongSummaryStatistics s = angajati(grafic).stream()
				.mapToLong(id -> grafic.tureAngajat(id).stream()
						.filter(t -> t.getTip() != TipTura.ZI || esteWeekend(t)).count())
				.summaryStatistics();
		return s.getCount() == 0 ? 0 : s.getMax() - s.getMin();
	}

	private static boolean esteWeekend(Tura t) {
		return t.getData().getDayOfWeek().getValue() >= 6;
	}

	private static Set<Integer> angajati(GraficLunar grafic) {
		return grafic.getTure().stream().flatMap(t -> t.asignariActive().stream())
				.map(Asignare::getIdAngajat).collect(Collectors.toCollection(TreeSet::new));
	}
}
