package org.medshift.domain.personal;

public enum StareSchimb {
	INITIATA,             // solicitantul a facut cererea
	ACCEPTATA_DE_COLEG,   // colegul a fost de acord
	IN_APROBARE,          // asteapta seful de sectie (aprobare conditionata)
	APROBATA,
	RESPINSA,
	ANULATA;
}
