package org.medshift.services.personal;

import org.medshift.domain.personal.Concediu;
import org.medshift.domain.personal.StareConcediu;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.Perioada;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Scope("singleton")
public class ConcediuRepositoryImpl extends RepositoryInMemorie<Concediu> implements IConcediuRepository {

	@Override protected Integer idOf(Concediu c) { return c.getIdConcediu(); }
	@Override protected void assignId(Concediu c, Integer id) { c.setIdConcediu(id); }
	@Override protected String numeEntitate() { return "Concediul"; }

	// regula A9: fara doua concedii active suprapuse pentru acelasi angajat
	@Override
	protected void verificaUnicitate(Concediu c) {
		findByExample(altul -> !altul.equals(c) && altul.seSuprapuneCu(c)).stream().findFirst().ifPresent(altul -> {
			throw new DomainException("Angajatul " + c.getIdAngajat() + " are deja un concediu in perioada "
					+ altul.getPerioada());
		});
	}

	@Override
	public List<Concediu> findByAngajat(Integer idAngajat) {
		return findByExample(c -> c.getIdAngajat().equals(idAngajat)).stream()
				.sorted(Comparator.comparing(c -> c.getPerioada().inceput())).toList();
	}

	@Override
	public List<Concediu> findAprobateInPerioada(Perioada perioada) {
		return findByExample(c -> c.getStare() == StareConcediu.APROBAT && c.getPerioada().seSuprapuneCu(perioada));
	}
}
