package org.medshift.services.personal;

import org.medshift.domain.personal.CerereSchimbTura;
import org.medshift.services.shared.RepositoryInMemorie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("singleton")
public class CerereSchimbTuraRepositoryImpl extends RepositoryInMemorie<CerereSchimbTura>
		implements ICerereSchimbTuraRepository {

	@Override protected Integer idOf(CerereSchimbTura c) { return c.getIdCerere(); }
	@Override protected void assignId(CerereSchimbTura c, Integer id) { c.setIdCerere(id); }
	@Override protected String numeEntitate() { return "Cererea de schimb"; }

	@Override
	public List<CerereSchimbTura> findByGrafic(Integer idGrafic) {
		return findByExample(c -> c.getIdGrafic().equals(idGrafic));
	}

	@Override
	public List<CerereSchimbTura> findDeschiseByAngajat(Integer idAngajat) {
		return findByExample(c -> c.esteDeschisa()
				&& (c.getIdSolicitant().equals(idAngajat) || c.getIdColeg().equals(idAngajat)));
	}
}
