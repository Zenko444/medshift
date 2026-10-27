package org.medshift.services.personal;

import org.medshift.domain.personal.Angajat;
import org.medshift.domain.personal.Email;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.GradMedical;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@Scope("singleton")
public class AngajatRepositoryImpl extends RepositoryInMemorie<Angajat> implements IAngajatRepository {

	@Override protected Integer idOf(Angajat a) { return a.getIdAngajat(); }
	@Override protected void assignId(Angajat a, Integer id) { a.setIdAngajat(id); }
	@Override protected String numeEntitate() { return "Angajatul"; }

	// regula V1: emailul este unic in sistem
	@Override
	protected void verificaUnicitate(Angajat a) {
		findByEmail(a.getEmail()).filter(existent -> !existent.equals(a)).ifPresent(existent -> {
			throw new DomainException("Emailul " + a.getEmail() + " este deja folosit de angajatul " + existent.getIdAngajat());
		});
	}

	@Override
	public List<Angajat> findBySectie(Integer idSectie) {
		return findByExample(a -> a.getIdSectie().equals(idSectie)).stream().sorted().toList();
	}

	@Override
	public List<Angajat> findBySectieSiGrad(Integer idSectie, GradMedical grad) {
		return findByExample(a -> a.getIdSectie().equals(idSectie) && a.getGrad() == grad).stream()
				.sorted(Comparator.comparing(Angajat::getIdAngajat)).toList();
	}

	@Override
	public Optional<Angajat> findByEmail(Email email) {
		return findByExample(a -> a.getEmail().equals(email)).stream().findFirst();
	}
}
