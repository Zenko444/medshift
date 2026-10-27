package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;

import java.util.List;

public interface IValidareGraficService {
	/** Erorile de validare JavaBean (adnotarile de pe entitati), ca mesaje. */
	List<String> valideazaCampuri(GraficLunar grafic);
	/** Incalcarile restrictiilor dure ale graficului (acoperire A1, suprapuneri A2, pauze A3). */
	List<String> valideazaRestrictii(GraficLunar grafic);
	/** Toate problemele: campuri + restrictii; lista goala = graficul poate fi publicat. */
	List<String> valideaza(GraficLunar grafic);
	boolean poateFiPublicat(GraficLunar grafic);
}
