package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** Value Object: cate persoane de un anumit grad sunt necesare pe un tip de tura (V8). */
public record CerintaAcoperire(@NotNull TipTura tipTura, @NotNull GradMedical grad,
		@Min(value = 0, message = "Numarul minim nu poate fi negativ.") @Max(value = 50, message = "Numar minim nerealist (max 50).") int numarMinim) implements Serializable {

	public CerintaAcoperire {
		if (tipTura == null || grad == null)
			throw new DomainException("Cerinta de acoperire trebuie sa aiba tip de tura si grad.");
		if (numarMinim < 0)
			throw new DomainException("Numarul minim nu poate fi negativ.");
		if (numarMinim < 1 && tipTura != TipTura.ZI)
			throw new DomainException("Turele de noapte si garda cer minim 1 persoana pe grad configurat.");
	}
}
