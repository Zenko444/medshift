package org.medshift.services.pontaj;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.shared.LunaCalendaristica;

import java.util.List;

public interface IFisaPontajFactory {
	FisaPontaj buildFisaGoala(Integer idAngajat, Integer idSectie, LunaCalendaristica luna);
	/** CF3.1: cate o fisa pentru fiecare angajat, din turele EFECTUATE ale unui grafic FINALIZAT. */
	List<FisaPontaj> buildFiseDinGrafic(GraficLunar graficFinalizat);
}
