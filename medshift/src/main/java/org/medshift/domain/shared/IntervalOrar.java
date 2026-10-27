package org.medshift.domain.shared;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/** Value Object (Shared Kernel): interval de timp imutabil, max 24 h (regula V6). */
public record IntervalOrar(LocalDateTime inceput, LocalDateTime sfarsit) implements Serializable {

	public IntervalOrar {
		if (inceput == null || sfarsit == null)
			throw new DomainException("Intervalul orar trebuie sa aiba inceput si sfarsit.");
		if (!sfarsit.isAfter(inceput))
			throw new DomainException("Sfarsitul intervalului trebuie sa fie dupa inceput.");
		if (Duration.between(inceput, sfarsit).toHours() > 24)
			throw new DomainException("O tura nu poate depasi 24 de ore.");
	}

	public double durataOre() {
		return Duration.between(inceput, sfarsit).toMinutes() / 60.0;
	}

	public boolean seSuprapuneCu(IntervalOrar alt) {
		return inceput.isBefore(alt.sfarsit) && alt.inceput.isBefore(sfarsit);
	}

	/** Orele dintre sfarsitul acestui interval si inceputul celuilalt (negativ = suprapunere). */
	public double pauzaPanaLa(IntervalOrar urmator) {
		return Duration.between(sfarsit, urmator.inceput).toMinutes() / 60.0;
	}
}
