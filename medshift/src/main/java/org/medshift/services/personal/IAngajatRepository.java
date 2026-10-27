package org.medshift.services.personal;

import org.medshift.domain.personal.Angajat;
import org.medshift.domain.personal.Email;
import org.medshift.domain.shared.GradMedical;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;
import java.util.Optional;

public interface IAngajatRepository extends IEntityRepository<Angajat> {
	List<Angajat> findBySectie(Integer idSectie);
	List<Angajat> findBySectieSiGrad(Integer idSectie, GradMedical grad);
	Optional<Angajat> findByEmail(Email email);
}
