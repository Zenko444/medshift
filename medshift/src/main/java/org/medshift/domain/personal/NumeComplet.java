package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** Value Object: numele unei persoane (regula V2: 2-60 de caractere fiecare). */
public record NumeComplet(
		@NotBlank @Size(min = 2, max = 60) String nume,
		@NotBlank @Size(min = 2, max = 60) String prenume) implements Serializable {

	public NumeComplet {
		if (nume == null || nume.isBlank() || prenume == null || prenume.isBlank())
			throw new DomainException("Numele si prenumele sunt obligatorii.");
		nume = nume.trim();
		prenume = prenume.trim();
		if (nume.length() < 2 || nume.length() > 60 || prenume.length() < 2 || prenume.length() > 60)
			throw new DomainException("Numele si prenumele trebuie sa aiba intre 2 si 60 de caractere.");
	}

	@Override
	public String toString() {
		return prenume + " " + nume;
	}
}
