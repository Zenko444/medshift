package org.medshift.domain.shared;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Value Object (Shared Kernel): luna pentru care se face graficul (regula V7). */
public record LunaCalendaristica(int an, int luna) implements Serializable, Comparable<LunaCalendaristica> {

	public LunaCalendaristica {
		if (luna < 1 || luna > 12)
			throw new DomainException("Luna trebuie sa fie intre 1 si 12.");
		if (an < 2000)
			throw new DomainException("An invalid: " + an);
	}

	public static LunaCalendaristica din(LocalDate data) {
		return new LunaCalendaristica(data.getYear(), data.getMonthValue());
	}

	public List<LocalDate> zile() {
		YearMonth ym = YearMonth.of(an, luna);
		return ym.atDay(1).datesUntil(ym.atEndOfMonth().plusDays(1)).toList();
	}

	public boolean contine(LocalDate data) {
		return data.getYear() == an && data.getMonthValue() == luna;
	}

	public LocalDate ultimaZi() {
		return YearMonth.of(an, luna).atEndOfMonth();
	}

	@Override
	public int compareTo(LunaCalendaristica alta) {
		return YearMonth.of(an, luna).compareTo(YearMonth.of(alta.an, alta.luna));
	}

	@Override
	public String toString() {
		return "%d-%02d".formatted(an, luna);
	}
}
