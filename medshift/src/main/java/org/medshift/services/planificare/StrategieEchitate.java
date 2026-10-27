package org.medshift.services.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.Tura;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Strategia de echitate: turele grele (noapte, garda, weekend) merg la cine are cele mai putine. */
@Component("echitate")
public class StrategieEchitate extends StrategieAbstracta {

	@Override
	protected Comparator<PersonalDisponibil> ordineCandidati(GraficLunar g, Tura tura) {
		Comparator<PersonalDisponibil> dupaTureGrele = Comparator.comparingLong(p -> numarTureGrele(g, p.idAngajat()));
		Comparator<PersonalDisponibil> dupaTotal = Comparator.comparingLong(p -> numarTure(g, p.idAngajat()));
		return (esteGrea(tura) ? dupaTureGrele.thenComparing(dupaTotal) : dupaTotal)
				.thenComparing(PersonalDisponibil::idAngajat);
	}
}
