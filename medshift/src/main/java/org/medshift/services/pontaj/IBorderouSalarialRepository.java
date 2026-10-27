package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.BorderouSalarial;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.IEntityRepository;

import java.util.Optional;

public interface IBorderouSalarialRepository extends IEntityRepository<BorderouSalarial> {
	Optional<BorderouSalarial> getBySectieSiLuna(Integer idSectie, LunaCalendaristica luna);
}
