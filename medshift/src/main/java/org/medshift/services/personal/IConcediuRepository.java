package org.medshift.services.personal;

import org.medshift.domain.personal.Concediu;
import org.medshift.domain.shared.Perioada;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;

public interface IConcediuRepository extends IEntityRepository<Concediu> {
	List<Concediu> findByAngajat(Integer idAngajat);
	/** Concediile aprobate care au macar o zi in perioada data (ex.: luna graficului). */
	List<Concediu> findAprobateInPerioada(Perioada perioada);
}
