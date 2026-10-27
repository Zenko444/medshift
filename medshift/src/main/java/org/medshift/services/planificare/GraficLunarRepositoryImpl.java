package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Repository in memorie pentru agregatul GraficLunar (ca ProjectEntityRepositoryImpl din T2.C3).
@Component
@Scope("singleton")
public class GraficLunarRepositoryImpl extends RepositoryInMemorie<GraficLunar> implements IGraficLunarRepository {

	@Override
	protected Integer idOf(GraficLunar g) {
		return g.getIdGrafic();
	}

	@Override
	protected void assignId(GraficLunar g, Integer id) {
		g.setIdGrafic(id);
	}

	@Override
	protected String numeEntitate() {
		return "Graficul";
	}

	// regula: un singur grafic pe sectie si luna (UQ(sectie_id, an, luna) din modelul SQL)
	@Override
	protected void verificaUnicitate(GraficLunar g) {
		getBySectieSiLuna(g.getIdSectie(), g.getLuna())
				.filter(existent -> !existent.equals(g))
				.ifPresent(existent -> {
					throw new DomainException("Exista deja un grafic pentru sectia " + g.getIdSectie()
							+ " in luna " + g.getLuna());
				});
	}

	@Override
	public Optional<GraficLunar> getBySectieSiLuna(Integer idSectie, LunaCalendaristica luna) {
		return findByExample(g -> g.getIdSectie().equals(idSectie) && g.getLuna().equals(luna)).stream().findFirst();
	}

	@Override
	public List<GraficLunar> findBySectie(Integer idSectie) {
		return findByExample(g -> g.getIdSectie().equals(idSectie)).stream()
				.sorted(Comparator.comparing(GraficLunar::getLuna)).toList();
	}
}
