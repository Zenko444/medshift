package org.medshift.domain.pontaj;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDate;

/** Value Object: o regula legala incalcata intr-o fisa de pontaj (ex.: "A3", "pauza de 6 h dupa garda"). */
public record Neconformitate(@NotBlank String regula, @NotBlank String descriere, LocalDate data) implements Serializable {
}
