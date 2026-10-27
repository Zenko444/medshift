package org.medshift.services.audit;

import java.time.LocalDateTime;

/** O intrare imuabila in jurnalul de audit (cerinta nefunctionala de securitate / GDPR). */
public record InregistrareAudit(LocalDateTime moment, String utilizator, String actiune, String entitate,
		String detalii) {
}
