package org.medshift.services.audit;

import java.util.List;

public interface IAuditService {
	void inregistreaza(String utilizator, String actiune, String entitate, String detalii);
	List<InregistrareAudit> istoric();
	List<InregistrareAudit> istoricPentru(String entitate);
}
