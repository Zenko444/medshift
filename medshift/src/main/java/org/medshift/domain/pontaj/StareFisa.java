package org.medshift.domain.pontaj;

public enum StareFisa {
	GENERATA,          // creata din turele efectuate
	NECONFORMA,        // verificarea legala a gasit probleme
	VALIDATA_LEGAL,    // trecuta de verificarea legala
	APROBATA_HR,       // aprobata de departamentul HR
	EXPORTATA;         // inclusa intr-un borderou trimis la salarizare
}
