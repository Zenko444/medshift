package org.medshift.services.planificare;

/** Linie din raportul de echitate (Computed Entity View). */
public record IndicatoriAngajat(Integer idAngajat, long totalTure, long nopti, long garzi, long weekenduri,
		double orePlanificate) {
}
