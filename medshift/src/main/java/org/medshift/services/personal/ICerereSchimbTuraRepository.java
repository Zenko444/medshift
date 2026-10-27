package org.medshift.services.personal;

import org.medshift.domain.personal.CerereSchimbTura;
import org.medshift.services.shared.IEntityRepository;

import java.util.List;

public interface ICerereSchimbTuraRepository extends IEntityRepository<CerereSchimbTura> {
	List<CerereSchimbTura> findByGrafic(Integer idGrafic);
	/** Cererile inca deschise in care angajatul e solicitant sau coleg. */
	List<CerereSchimbTura> findDeschiseByAngajat(Integer idAngajat);
}
