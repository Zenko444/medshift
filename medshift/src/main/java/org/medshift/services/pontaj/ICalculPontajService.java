package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.TotalOre;
import org.medshift.domain.shared.LunaCalendaristica;

import java.util.Map;

/** Computation Service pentru view-urile de pontaj (sectiunea 5.4 din Sprint 1). */
public interface ICalculPontajService {
	TotalOre totaluri(FisaPontaj fisa);                                          // C5
	double oreSuplimentare(FisaPontaj fisa, int normaOreLunare);                 // C6
	Map<Integer, TotalOre> sumarLunarSectie(Integer idSectie, LunaCalendaristica luna);   // view: angajat -> totaluri
	TotalOre totalSectie(Integer idSectie, LunaCalendaristica luna);
}
