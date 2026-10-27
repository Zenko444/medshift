package org.medshift.services.personal;

import org.medshift.domain.personal.DoleanteLunare;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;
import java.util.Optional;

public interface IDoleanteLunareRepository extends IEntityRepository<DoleanteLunare> {
	Optional<DoleanteLunare> getByAngajatSiLuna(Integer idAngajat, LunaCalendaristica luna);
	List<DoleanteLunare> findByLuna(LunaCalendaristica luna);
}
