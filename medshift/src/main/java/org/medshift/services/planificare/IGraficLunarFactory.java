package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.MatriceAcoperire;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.TipTura;

import java.time.LocalDate;
import java.util.Set;

public interface IGraficLunarFactory {
	// grafic fara ture (doar structura lunii)
	GraficLunar buildGraficGol(Integer idSectie, LunaCalendaristica luna, LocalDate termenDoleante);
	// grafic cu cate o tura din fiecare tip cerut, pentru fiecare zi a lunii + matricea de acoperire
	GraficLunar buildGraficCuTure(Integer idSectie, LunaCalendaristica luna, LocalDate termenDoleante,
			Set<TipTura> tipuriTure, MatriceAcoperire matrice);
	IGraficLunarRepository getGraficLunarRepository();
}
