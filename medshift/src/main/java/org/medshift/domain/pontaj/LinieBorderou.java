package org.medshift.domain.pontaj;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** Value Object: un rand din borderou = totalurile unui angajat pentru luna respectiva. */
public record LinieBorderou(@NotNull Integer idAngajat, @NotNull Integer idFisa, @NotNull TotalOre totaluri)
		implements Serializable {
}
