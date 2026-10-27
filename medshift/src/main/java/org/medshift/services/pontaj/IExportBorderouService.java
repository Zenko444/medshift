package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.BorderouSalarial;

/** CF3.5: genereaza fisierul structurat pentru softul de salarizare. Acelasi borderou = acelasi continut (R5). */
public interface IExportBorderouService {
	String exporta(BorderouSalarial borderou);
	String numeFisier(BorderouSalarial borderou);
}
