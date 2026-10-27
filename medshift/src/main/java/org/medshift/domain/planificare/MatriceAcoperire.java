package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/** Value Object: matricea de acoperire minima a sectiei (CF1.2). Imutabila. */
public record MatriceAcoperire(@NotNull @Valid List<CerintaAcoperire> cerinte) implements Serializable {

	public MatriceAcoperire {
		cerinte = List.copyOf(cerinte);
		long distincte = cerinte.stream().map(c -> c.tipTura() + "/" + c.grad()).distinct().count();
		if (distincte != cerinte.size())
			throw new DomainException("Matricea contine doua cerinte pentru acelasi tip de tura si grad.");
	}

	public static MatriceAcoperire goala() {
		return new MatriceAcoperire(List.of());
	}

	public int necesarPentru(TipTura tip, GradMedical grad) {
		return cerinte.stream()
				.filter(c -> c.tipTura() == tip && c.grad() == grad)
				.mapToInt(CerintaAcoperire::numarMinim)
				.findFirst().orElse(0);
	}

	public int necesarTotal(TipTura tip) {
		return cerinte.stream().filter(c -> c.tipTura() == tip).mapToInt(CerintaAcoperire::numarMinim).sum();
	}

	public List<CerintaAcoperire> cerintePentru(TipTura tip) {
		return cerinte.stream().filter(c -> c.tipTura() == tip).toList();
	}

	public boolean esteConfigurata() {
		return !cerinte.isEmpty();
	}
}
