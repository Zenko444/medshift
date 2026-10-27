package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;
import java.util.Optional;

public interface IGraficLunarRepository extends IEntityRepository<GraficLunar> {
	// cautari specifice agregatului GraficLunar (pe langa cele comune din IEntityRepository)
	Optional<GraficLunar> getBySectieSiLuna(Integer idSectie, LunaCalendaristica luna);
	List<GraficLunar> findBySectie(Integer idSectie);
}
