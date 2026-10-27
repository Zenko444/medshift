package org.medshift.services.pontaj;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.medshift.domain.pontaj.CategorieOre;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.LiniePontaj;
import org.medshift.domain.pontaj.Neconformitate;
import org.medshift.domain.pontaj.SegmentOre;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * Reguli verificate (de confirmat cu Regulamentul de Ordine Interioara al spitalului):
 *  - A3: pauza minima dupa fiecare tura (24 h dupa garda, 12 h dupa celelalte)
 *  - CM-114: media de maxim 48 h de program normal pe saptamana, calculata pe luna (fara orele de garda)
 */
@Service
public class ConformitateServiceImpl implements IConformitateService {

	public static final double MAXIM_ORE_PE_SAPTAMANA = 48;

	private final Validator validator;

	public ConformitateServiceImpl(Validator validator) {
		this.validator = validator;
	}

	@Override
	public List<String> valideazaCampuri(FisaPontaj fisa) {
		List<String> erori = new ArrayList<>();
		for (ConstraintViolation<FisaPontaj> v : validator.validate(fisa))
			erori.add(v.getPropertyPath() + ": " + v.getMessage());
		return erori;
	}

	@Override
	public List<Neconformitate> verifica(FisaPontaj fisa) {
		List<Neconformitate> rezultat = new ArrayList<>();
		valideazaCampuri(fisa).forEach(e -> rezultat.add(new Neconformitate("CAMP", e, null)));

		List<LiniePontaj> linii = fisa.getLinii().stream()
				.sorted(Comparator.comparing(l -> l.getInterval().inceput())).toList();
		// A3: pauza dupa fiecare tura
		for (int i = 1; i < linii.size(); i++) {
			LiniePontaj prev = linii.get(i - 1), cur = linii.get(i);
			double pauza = Duration.between(prev.getInterval().sfarsit(), cur.getInterval().inceput()).toMinutes() / 60.0;
			int minim = prev.getTipTura().pauzaMinimaOre();
			if (pauza < minim)
				rezultat.add(new Neconformitate("A3", "Doar " + pauza + " h pauza dupa " + prev.getTipTura()
						+ " din " + prev.getData() + " (minim " + minim + " h).", cur.getData()));
		}
		// CM-114: media saptamanala a orelor de program normal (fara garda), calculata pe luna fisei
		double ore = linii.stream().flatMap(l -> l.getSegmente().stream())
				.filter(seg -> seg.categorie() != CategorieOre.GARDA).mapToDouble(SegmentOre::ore).sum();
		double saptamani = fisa.getLuna().zile().size() / 7.0;
		double medie = Math.round(ore / saptamani * 10) / 10.0;
		if (medie > MAXIM_ORE_PE_SAPTAMANA)
			rezultat.add(new Neconformitate("CM-114", "Media de " + medie + " h pe saptamana depaseste "
					+ MAXIM_ORE_PE_SAPTAMANA + " h.", null));
		return rezultat;
	}
}
