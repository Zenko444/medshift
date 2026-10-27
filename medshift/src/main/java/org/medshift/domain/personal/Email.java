package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Locale;

/** Value Object: o adresa de email valida (regula V1). Se pastreaza cu litere mici. */
public record Email(@NotBlank @jakarta.validation.constraints.Email String adresa) implements Serializable {

	private static final String FORMAT = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}";

	public Email {
		if (adresa == null)
			throw new DomainException("Adresa de email este obligatorie.");
		adresa = adresa.trim().toLowerCase(Locale.ROOT);
		if (!adresa.matches(FORMAT))
			throw new DomainException("Adresa de email invalida: " + adresa);
	}

	@Override
	public String toString() {
		return adresa;
	}
}
