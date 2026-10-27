package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.TotalOre;
import org.medshift.domain.shared.LunaCalendaristica;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

@Service
public class CalculPontajServiceImpl implements ICalculPontajService {

	private final IFisaPontajRepository fise;

	public CalculPontajServiceImpl(IFisaPontajRepository fise) {
		this.fise = fise;
	}

	@Override
	public TotalOre totaluri(FisaPontaj fisa) {
		return fisa.totaluri();
	}

	@Override
	public double oreSuplimentare(FisaPontaj fisa, int normaOreLunare) {
		return fisa.totaluri().oreSuplimentare(normaOreLunare);
	}

	@Override
	public Map<Integer, TotalOre> sumarLunarSectie(Integer idSectie, LunaCalendaristica luna) {
		Map<Integer, TotalOre> sumar = new TreeMap<>();
		fise.findBySectieSiLuna(idSectie, luna).forEach(f -> sumar.put(f.getIdAngajat(), f.totaluri()));
		return sumar;
	}

	@Override
	public TotalOre totalSectie(Integer idSectie, LunaCalendaristica luna) {
		return sumarLunarSectie(idSectie, luna).values().stream().reduce(TotalOre.ZERO, TotalOre::plus);
	}
}
