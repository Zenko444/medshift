package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.BorderouSalarial;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("singleton")
public class BorderouSalarialRepositoryImpl extends RepositoryInMemorie<BorderouSalarial>
		implements IBorderouSalarialRepository {

	@Override protected Integer idOf(BorderouSalarial b) { return b.getIdBorderou(); }
	@Override protected void assignId(BorderouSalarial b, Integer id) { b.setIdBorderou(id); }
	@Override protected String numeEntitate() { return "Borderoul"; }

	// regula R5: un singur borderou pe sectie si luna (exportul e idempotent)
	@Override
	protected void verificaUnicitate(BorderouSalarial b) {
		getBySectieSiLuna(b.getIdSectie(), b.getLuna()).filter(existent -> !existent.equals(b)).ifPresent(existent -> {
			throw new DomainException("Exista deja un borderou pentru sectia " + b.getIdSectie() + " in luna " + b.getLuna());
		});
	}

	@Override
	public Optional<BorderouSalarial> getBySectieSiLuna(Integer idSectie, LunaCalendaristica luna) {
		return findByExample(b -> b.getIdSectie().equals(idSectie) && b.getLuna().equals(luna)).stream().findFirst();
	}
}
