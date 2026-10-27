package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.StareFisa;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;
import java.util.Optional;

public interface IFisaPontajRepository extends IEntityRepository<FisaPontaj> {
	/** Un angajat are cate o fisa pentru fiecare sectie in care a lucrat in luna respectiva. */
	Optional<FisaPontaj> getByAngajatSectieSiLuna(Integer idAngajat, Integer idSectie, LunaCalendaristica luna);
	List<FisaPontaj> findByLuna(LunaCalendaristica luna);
	List<FisaPontaj> findBySectieSiLuna(Integer idSectie, LunaCalendaristica luna);
	List<FisaPontaj> findByLunaSiStare(LunaCalendaristica luna, StareFisa stare);
}
