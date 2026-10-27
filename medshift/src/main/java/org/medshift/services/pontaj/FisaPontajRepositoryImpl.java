package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.StareFisa;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("singleton")
public class FisaPontajRepositoryImpl extends RepositoryInMemorie<FisaPontaj> implements IFisaPontajRepository {

	@Override protected Integer idOf(FisaPontaj f) { return f.getIdFisa(); }
	@Override protected void assignId(FisaPontaj f, Integer id) { f.setIdFisa(id); }
	@Override protected String numeEntitate() { return "Fisa de pontaj"; }

	// o singura fisa pe angajat, sectie si luna
	@Override
	protected void verificaUnicitate(FisaPontaj f) {
		getByAngajatSectieSiLuna(f.getIdAngajat(), f.getIdSectie(), f.getLuna()).filter(existent -> !existent.equals(f)).ifPresent(existent -> {
			throw new DomainException("Angajatul " + f.getIdAngajat() + " are deja fisa de pontaj pentru sectia "
					+ f.getIdSectie() + " si luna " + f.getLuna());
		});
	}

	@Override
	public Optional<FisaPontaj> getByAngajatSectieSiLuna(Integer idAngajat, Integer idSectie, LunaCalendaristica luna) {
		return findByExample(f -> f.getIdAngajat().equals(idAngajat) && f.getIdSectie().equals(idSectie)
				&& f.getLuna().equals(luna)).stream().findFirst();
	}

	@Override
	public List<FisaPontaj> findByLuna(LunaCalendaristica luna) {
		return findByExample(f -> f.getLuna().equals(luna));
	}

	@Override
	public List<FisaPontaj> findBySectieSiLuna(Integer idSectie, LunaCalendaristica luna) {
		return findByExample(f -> f.getIdSectie().equals(idSectie) && f.getLuna().equals(luna));
	}

	@Override
	public List<FisaPontaj> findByLunaSiStare(LunaCalendaristica luna, StareFisa stare) {
		return findByExample(f -> f.getLuna().equals(luna) && f.getStare() == stare);
	}
}
