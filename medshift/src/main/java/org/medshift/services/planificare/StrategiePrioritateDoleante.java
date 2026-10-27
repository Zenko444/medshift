package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.Tura;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Strategia cu prioritate pe doleante: cine a cerut liber intr-o zi nu e pus pe tura in acea zi
 * (cat timp exista alti candidati); intre ceilalti, castiga cine are mai putine ture.
 */
@Component("prioritateDoleante")
public class StrategiePrioritateDoleante extends StrategieAbstracta {

	@Override
	protected Comparator<PersonalDisponibil> ordineCandidati(GraficLunar g, Tura tura) {
		return Comparator.<PersonalDisponibil, Boolean>comparing(p -> p.preferaLiber(tura.getData()))
				.thenComparingLong(p -> numarTure(g, p.idAngajat()))
				.thenComparing(PersonalDisponibil::idAngajat);
	}
}
