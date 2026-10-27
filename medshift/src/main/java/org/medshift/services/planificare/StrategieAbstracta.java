package org.medshift.services.planificare;

import org.medshift.domain.planificare.CerintaAcoperire;
import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.Tura;

import java.util.Comparator;
import java.util.List;

/**
 * Algoritm greedy comun: ia turele in ordine cronologica si, pentru fiecare cerinta din matrice,
 * alege candidatii eligibili in ordinea data de strategie. Regulile dure sunt verificate de GraficLunar.
 */
abstract class StrategieAbstracta implements StrategieOptimizare {

	/** Ordinea in care sunt incercati candidatii pentru o tura (aici difera strategiile). */
	protected abstract Comparator<PersonalDisponibil> ordineCandidati(GraficLunar grafic, Tura tura);

	/** Daca strategia respecta "prefer liber" ca restrictie (true) sau doar ca preferinta de ordonare. */
	protected boolean excludeCeiCarePreferaLiber() {
		return false;
	}

	@Override
	public void genereaza(GraficLunar grafic, List<PersonalDisponibil> personal) {
		List<Tura> ture = grafic.getTure().stream()
				.sorted(Comparator.comparing(t -> t.getInterval().inceput())).toList();
		for (Tura tura : ture) {
			for (CerintaAcoperire c : grafic.getMatrice().cerintePentru(tura.getTip())) {
				long lipsa = c.numarMinim() - tura.numarAsignati(c.grad());
				List<PersonalDisponibil> candidati = personal.stream()
						.filter(p -> p.grad() == c.grad())
						.filter(p -> p.esteDisponibil(tura.getData()))
						.filter(p -> !excludeCeiCarePreferaLiber() || !p.preferaLiber(tura.getData()))
						.sorted(ordineCandidati(grafic, tura))
						.toList();
				for (PersonalDisponibil p : candidati) {
					if (lipsa <= 0)
						break;
					if (grafic.poateAsigna(tura.getIdTura(), p.idAngajat())) {
						grafic.asigneaza(tura.getIdTura(), p.idAngajat(), p.grad());
						lipsa--;
					}
				}
			}
		}
	}

	protected static long numarTure(GraficLunar g, Integer idAngajat) {
		return g.tureAngajat(idAngajat).size();
	}

	protected static long numarTureGrele(GraficLunar g, Integer idAngajat) {
		return g.tureAngajat(idAngajat).stream().filter(StrategieAbstracta::esteGrea).count();
	}

	/** Tura "grea": noapte, garda sau in weekend. */
	protected static boolean esteGrea(Tura t) {
		return t.getTip() != org.medshift.domain.shared.TipTura.ZI
				|| t.getData().getDayOfWeek().getValue() >= 6;
	}
}
