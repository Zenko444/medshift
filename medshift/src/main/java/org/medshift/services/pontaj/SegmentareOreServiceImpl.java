package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.CategorieOre;
import org.medshift.domain.pontaj.SegmentOre;
import org.medshift.domain.shared.CalendarSarbatoriLegale;
import org.medshift.domain.shared.IntervalOrar;
import org.medshift.domain.shared.TipTura;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fiecare ora lucrata intra in EXACT o categorie, dupa ordinea de prioritate:
 *   1. SARBATOARE_LEGALA  (ziua e sarbatoare legala)
 *   2. WEEKEND            (sambata sau duminica)
 *   3. NOAPTE             (intre 22:00 si 06:00)
 *   4. GARDA              (restul orelor unei ture de garda)
 *   5. ZI                 (restul)
 * Prioritatea e o regula de business propusa de echipa; se poate schimba aici, intr-un singur loc.
 */
@Service
public class SegmentareOreServiceImpl implements ISegmentareOreService {

	public static final LocalTime INCEPUT_NOAPTE = LocalTime.of(22, 0);
	public static final LocalTime SFARSIT_NOAPTE = LocalTime.of(6, 0);

	@Override
	public List<SegmentOre> segmenteaza(IntervalOrar interval, TipTura tipTura) {
		Map<CategorieOre, Long> minute = new EnumMap<>(CategorieOre.class);
		LocalDateTime t = interval.inceput();
		while (t.isBefore(interval.sfarsit())) {
			LocalDateTime urmatorul = min(urmatoareaGranita(t), interval.sfarsit());
			minute.merge(categorie(t, tipTura), Duration.between(t, urmatorul).toMinutes(), Long::sum);
			t = urmatorul;
		}
		List<SegmentOre> segmente = new ArrayList<>();
		minute.forEach((cat, min) -> segmente.add(new SegmentOre(cat, min / 60.0)));
		return segmente;
	}

	/** Categoria momentului t (toata bucata pana la urmatoarea granita are aceeasi categorie). */
	static CategorieOre categorie(LocalDateTime t, TipTura tipTura) {
		if (CalendarSarbatoriLegale.esteSarbatoare(t.toLocalDate()))
			return CategorieOre.SARBATOARE_LEGALA;
		if (CalendarSarbatoriLegale.esteWeekend(t.toLocalDate()))
			return CategorieOre.WEEKEND;
		LocalTime ora = t.toLocalTime();
		if (!ora.isBefore(INCEPUT_NOAPTE) || ora.isBefore(SFARSIT_NOAPTE))
			return CategorieOre.NOAPTE;
		return tipTura == TipTura.GARDA ? CategorieOre.GARDA : CategorieOre.ZI;
	}

	/** Urmatorul moment in care se poate schimba categoria: 06:00, 22:00 sau miezul noptii. */
	static LocalDateTime urmatoareaGranita(LocalDateTime t) {
		LocalDateTime zi = t.toLocalDate().atStartOfDay();
		for (LocalDateTime g : List.of(zi.plusHours(6), zi.plusHours(22), zi.plusDays(1)))
			if (g.isAfter(t))
				return g;
		return zi.plusDays(1);
	}

	private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
		return a.isBefore(b) ? a : b;
	}
}
