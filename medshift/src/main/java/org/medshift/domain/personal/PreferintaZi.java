package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.TipTura;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

/** Value Object: o doleanta pentru o zi. tipTura e completat doar pentru PREFER_TURA. */
public record PreferintaZi(@NotNull LocalDate data, @NotNull TipPreferinta tip, TipTura tipTura) implements Serializable {

	public PreferintaZi {
		if (data == null || tip == null)
			throw new DomainException("Doleanta trebuie sa aiba data si tipul.");
		if (tip == TipPreferinta.PREFER_TURA && tipTura == null)
			throw new DomainException("Pentru PREFER_TURA trebuie precizat tipul turei.");
		if (tip != TipPreferinta.PREFER_TURA && tipTura != null)
			throw new DomainException("Tipul turei se completeaza doar pentru PREFER_TURA.");
	}
}
