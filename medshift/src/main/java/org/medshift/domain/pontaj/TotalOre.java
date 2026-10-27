package org.medshift.domain.pontaj;

import java.io.Serializable;
import java.util.Collection;

/** Value Object: totalurile de ore pe categorii (regula C5). Se calculeaza, nu se introduce de mana. */
public record TotalOre(double oreZi, double oreNoapte, double oreWeekend, double oreGarda, double oreSarbatoare)
		implements Serializable {

	public static final TotalOre ZERO = new TotalOre(0, 0, 0, 0, 0);

	public static TotalOre din(Collection<SegmentOre> segmente) {
		double zi = 0, noapte = 0, weekend = 0, garda = 0, sarbatoare = 0;
		for (SegmentOre s : segmente) {
			switch (s.categorie()) {
				case ZI -> zi += s.ore();
				case NOAPTE -> noapte += s.ore();
				case WEEKEND -> weekend += s.ore();
				case GARDA -> garda += s.ore();
				case SARBATOARE_LEGALA -> sarbatoare += s.ore();
			}
		}
		return new TotalOre(zi, noapte, weekend, garda, sarbatoare);
	}

	public double total() {
		return oreZi + oreNoapte + oreWeekend + oreGarda + oreSarbatoare;
	}

	public TotalOre plus(TotalOre alt) {
		return new TotalOre(oreZi + alt.oreZi, oreNoapte + alt.oreNoapte, oreWeekend + alt.oreWeekend,
				oreGarda + alt.oreGarda, oreSarbatoare + alt.oreSarbatoare);
	}

	/** Regula C6: orele peste norma lunara. */
	public double oreSuplimentare(int normaOreLunare) {
		return Math.max(0, total() - normaOreLunare);
	}
}
