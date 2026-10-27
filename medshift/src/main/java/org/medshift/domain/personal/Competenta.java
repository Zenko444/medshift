package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/** Value Object: o competenta speciala (ex.: "ECO" - ecografie, "VENT" - ventilatie mecanica). */
public record Competenta(@NotBlank String cod, @NotBlank String denumire) implements Serializable {

	public Competenta {
		if (cod == null || cod.isBlank() || denumire == null || denumire.isBlank())
			throw new DomainException("Competenta trebuie sa aiba cod si denumire.");
		cod = cod.trim().toUpperCase();
	}
}
