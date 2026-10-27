package org.medshift.domain.shared;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sarbatorile legale din Romania (Codul Muncii, art. 139), folosite la pontaj (ore de sarbatoare)
 * si la calculul zilelor lucratoare de concediu. Clasa nu are stare: doar metode statice.
 * Lista trebuie verificata anual (legislatia se poate schimba).
 */
public final class CalendarSarbatoriLegale {

	private CalendarSarbatoriLegale() {
	}

	public static Set<LocalDate> sarbatori(int an) {
		Set<LocalDate> s = new TreeSet<>();
		s.add(LocalDate.of(an, 1, 1));
		s.add(LocalDate.of(an, 1, 2));
		s.add(LocalDate.of(an, 1, 6));     // Boboteaza (din 2024)
		s.add(LocalDate.of(an, 1, 7));     // Sf. Ioan (din 2024)
		s.add(LocalDate.of(an, 1, 24));    // Unirea Principatelor
		LocalDate pasti = pasteOrtodox(an);
		s.add(pasti.minusDays(2));         // Vinerea Mare
		s.add(pasti);                      // Pastele
		s.add(pasti.plusDays(1));          // a doua zi de Paste
		s.add(LocalDate.of(an, 5, 1));
		s.add(LocalDate.of(an, 6, 1));     // Ziua Copilului
		s.add(pasti.plusDays(49));         // Rusaliile
		s.add(pasti.plusDays(50));         // a doua zi de Rusalii
		s.add(LocalDate.of(an, 8, 15));    // Adormirea Maicii Domnului
		s.add(LocalDate.of(an, 11, 30));   // Sf. Andrei
		s.add(LocalDate.of(an, 12, 1));    // Ziua Nationala
		s.add(LocalDate.of(an, 12, 25));
		s.add(LocalDate.of(an, 12, 26));
		return s;
	}

	public static boolean esteSarbatoare(LocalDate data) {
		return sarbatori(data.getYear()).contains(data);
	}

	public static boolean esteWeekend(LocalDate data) {
		return data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY;
	}

	public static boolean esteZiLucratoare(LocalDate data) {
		return !esteWeekend(data) && !esteSarbatoare(data);
	}

	/** Pastele ortodox: algoritmul lui Meeus pentru calendarul iulian, convertit in calendarul gregorian. */
	public static LocalDate pasteOrtodox(int an) {
		int a = an % 4, b = an % 7, c = an % 19;
		int d = (19 * c + 15) % 30;
		int e = (2 * a + 4 * b - d + 34) % 7;
		int luna = (d + e + 114) / 31;
		int zi = (d + e + 114) % 31 + 1;
		int diferentaCalendare = an / 100 - an / 400 - 2;    // 13 zile in secolele XX-XXI
		return LocalDate.of(an, luna, zi).plusDays(diferentaCalendare);
	}
}
