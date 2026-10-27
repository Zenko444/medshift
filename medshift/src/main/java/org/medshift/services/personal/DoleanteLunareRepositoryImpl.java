package org.medshift.services.personal;

import org.medshift.domain.personal.DoleanteLunare;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("singleton")
public class DoleanteLunareRepositoryImpl extends RepositoryInMemorie<DoleanteLunare> implements IDoleanteLunareRepository {

	@Override protected Integer idOf(DoleanteLunare d) { return d.getIdDoleante(); }
	@Override protected void assignId(DoleanteLunare d, Integer id) { d.setIdDoleante(id); }
	@Override protected String numeEntitate() { return "Doleantele"; }

	// regula A9: o singura colectie de doleante pe angajat si luna
	@Override
	protected void verificaUnicitate(DoleanteLunare d) {
		getByAngajatSiLuna(d.getIdAngajat(), d.getLuna()).filter(existent -> !existent.equals(d)).ifPresent(existent -> {
			throw new DomainException("Angajatul " + d.getIdAngajat() + " are deja doleante pentru luna " + d.getLuna());
		});
	}

	@Override
	public Optional<DoleanteLunare> getByAngajatSiLuna(Integer idAngajat, LunaCalendaristica luna) {
		return findByExample(d -> d.getIdAngajat().equals(idAngajat) && d.getLuna().equals(luna)).stream().findFirst();
	}

	@Override
	public List<DoleanteLunare> findByLuna(LunaCalendaristica luna) {
		return findByExample(d -> d.getLuna().equals(luna));
	}
}
