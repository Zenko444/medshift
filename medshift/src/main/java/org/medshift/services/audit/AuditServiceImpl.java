package org.medshift.services.audit;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

// Audit Service: jurnal append-only (nu se pot modifica sau sterge intrari); in memorie pana la sprintul JPA.
@Service
public class AuditServiceImpl implements IAuditService {
	private static final Logger logger = Logger.getLogger(AuditServiceImpl.class.getName());

	private final List<InregistrareAudit> jurnal = Collections.synchronizedList(new ArrayList<>());
	private final Clock clock;

	public AuditServiceImpl(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void inregistreaza(String utilizator, String actiune, String entitate, String detalii) {
		InregistrareAudit i = new InregistrareAudit(LocalDateTime.now(clock), utilizator, actiune, entitate, detalii);
		jurnal.add(i);
		logger.info("AUDIT " + i);
	}

	@Override
	public List<InregistrareAudit> istoric() {
		return List.copyOf(jurnal);
	}

	@Override
	public List<InregistrareAudit> istoricPentru(String entitate) {
		return jurnal.stream().filter(i -> i.entitate().equals(entitate)).toList();
	}
}
