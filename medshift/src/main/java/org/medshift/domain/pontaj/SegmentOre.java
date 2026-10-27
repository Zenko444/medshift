package org.medshift.domain.pontaj;

import org.medshift.domain.shared.DomainException;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** Value Object: o bucata din orele unei ture, intr-o singura categorie (ex.: 4 ore de NOAPTE). */
public record SegmentOre(@NotNull CategorieOre categorie, @Min(0) double ore) implements Serializable {

	public SegmentOre {
		if (categorie == null)
			throw new DomainException("Segmentul trebuie sa aiba o categorie.");
		if (ore < 0 || ore > 24)
			throw new DomainException("Orele unui segment trebuie sa fie intre 0 si 24 (regula V10).");
	}
}
