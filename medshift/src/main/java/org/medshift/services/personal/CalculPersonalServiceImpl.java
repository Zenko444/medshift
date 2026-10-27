package org.medshift.services.personal;

import org.medshift.domain.personal.Concediu;
import org.medshift.domain.personal.DoleanteLunare;
import org.medshift.domain.personal.StareConcediu;
import org.medshift.domain.personal.TipConcediu;
import org.medshift.domain.shared.CalendarSarbatoriLegale;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

// Domain Computation Service pentru contextul Personal.
@Service
public class CalculPersonalServiceImpl implements ICalculPersonalService {

	/** Minimul legal de concediu de odihna: 20 de zile lucratoare pe an (Codul Muncii, art. 145). De confirmat cu HR. */
	public static final int ZILE_CONCEDIU_ODIHNA_PE_AN = 20;

	private final IConcediuRepository concedii;

	public CalculPersonalServiceImpl(IConcediuRepository concedii) {
		this.concedii = concedii;
	}

	@Override
	public long zileLucratoareConcediuOdihna(Integer idAngajat, int an) {
		return concedii.findByAngajat(idAngajat).stream()
				.filter(c -> c.getTip() == TipConcediu.ODIHNA && c.getStare() == StareConcediu.APROBAT)
				.mapToLong(c -> zileLucratoareInAn(c, an))
				.sum();
	}

	@Override
	public long zileConcediuOdihnaRamase(Integer idAngajat, int an) {
		return Math.max(0, ZILE_CONCEDIU_ODIHNA_PE_AN - zileLucratoareConcediuOdihna(idAngajat, an));
	}

	@Override
	public double procentDoleanteRespectate(DoleanteLunare doleante, Set<LocalDate> zileLucrate) {
		Set<LocalDate> cerute = doleante.zilePreferaLiber();
		if (cerute.isEmpty())
			return 100.0;
		long respectate = cerute.stream().filter(zi -> !zileLucrate.contains(zi)).count();
		return 100.0 * respectate / cerute.size();
	}

	/** Zilele lucratoare (fara weekend si sarbatori legale) din concediu care cad in anul dat. */
	private static long zileLucratoareInAn(Concediu c, int an) {
		return c.getPerioada().inceput().datesUntil(c.getPerioada().sfarsit().plusDays(1))
				.filter(zi -> zi.getYear() == an)
				.filter(CalendarSarbatoriLegale::esteZiLucratoare)
				.count();
	}
}
