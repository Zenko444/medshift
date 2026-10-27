package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.Tura;

import java.util.List;
import java.util.Map;

public interface ICalculPlanificareService {
	// Computed entity fields
	double gradAcoperire(GraficLunar grafic, Integer idTura);          // C2: 0.0 - 1.0
	double orePlanificate(GraficLunar grafic, Integer idAngajat);      // suma duratelor turelor
	// Computed entity views
	double gradAcoperireGrafic(GraficLunar grafic);                    // media pe toate turele
	List<Tura> raportTureSubNecesar(GraficLunar grafic);               // raport acoperire
	List<IndicatoriAngajat> raportEchitate(GraficLunar grafic);        // raport echitate
	Map<Integer, Long> distributieGarziWeekend(GraficLunar grafic);
	long diferentaMaximaTureGrele(GraficLunar grafic);                 // max - min ture grele intre angajati
}
