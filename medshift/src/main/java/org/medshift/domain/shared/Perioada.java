package org.medshift.domain.shared;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Value Object (Shared Kernel): un interval de zile calendaristice, capete incluse (regula V5). */
public record Perioada(@NotNull LocalDate inceput, @NotNull LocalDate sfarsit) implements Serializable {

	public Perioada {
		if (inceput == null || sfarsit == null)
			throw new DomainException("Perioada trebuie sa aiba data de inceput si de sfarsit.");
		if (sfarsit.isBefore(inceput))
			throw new DomainException("Data de sfarsit (" + sfarsit + ") este inaintea datei de inceput (" + inceput + ").");
	}

	public boolean contine(LocalDate data) {
		return !data.isBefore(inceput) && !data.isAfter(sfarsit);
	}

	public boolean seSuprapuneCu(Perioada alta) {
		return !inceput.isAfter(alta.sfarsit) && !alta.inceput.isAfter(sfarsit);
	}

	public long numarZile() {
		return ChronoUnit.DAYS.between(inceput, sfarsit) + 1;
	}
}
