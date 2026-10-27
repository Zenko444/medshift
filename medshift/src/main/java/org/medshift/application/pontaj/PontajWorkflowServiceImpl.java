package org.medshift.application.pontaj;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.pontaj.*;
import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.services.audit.IAuditService;
import org.medshift.services.planificare.IGraficLunarRepository;
import org.medshift.services.pontaj.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

/** Stratul Application pentru Pontaj: graficul finalizat -> fise -> verificare -> aprobare HR -> borderou -> export. */
@Service
public class PontajWorkflowServiceImpl implements IPontajWorkflowService {
	private static final Logger logger = Logger.getLogger(PontajWorkflowServiceImpl.class.getName());

	private final IGraficLunarRepository grafice;     // doar citire: graficul finalizat
	private final IFisaPontajRepository fise;
	private final IFisaPontajFactory factory;
	private final IConformitateService conformitate;
	private final IBorderouSalarialRepository borderouri;
	private final IExportBorderouService export;
	private final IAuditService audit;

	public PontajWorkflowServiceImpl(IGraficLunarRepository grafice, IFisaPontajRepository fise,
			IFisaPontajFactory factory, IConformitateService conformitate, IBorderouSalarialRepository borderouri,
			IExportBorderouService export, IAuditService audit) {
		this.grafice = grafice;
		this.fise = fise;
		this.factory = factory;
		this.conformitate = conformitate;
		this.borderouri = borderouri;
		this.export = export;
		this.audit = audit;
		logger.info(">>> BEAN: PontajWorkflowServiceImpl instantiated!");
	}

	@Override
	public List<FisaPontaj> genereazaFise(String utilizator, Integer idGrafic) {
		GraficLunar g = grafice.getById(idGrafic);
		if (!fise.findBySectieSiLuna(g.getIdSectie(), g.getLuna()).isEmpty())
			throw new DomainException("Fisele pentru sectia " + g.getIdSectie() + " si luna " + g.getLuna()
					+ " au fost deja generate.");
		List<FisaPontaj> generate = factory.buildFiseDinGrafic(g);
		fise.addAll(generate);
		audit.inregistreaza(utilizator, "FISE_GENERATE", "GraficLunar#" + idGrafic, generate.size() + " fise");
		return generate;
	}

	@Override
	public List<Neconformitate> verificaFisa(String utilizator, Integer idFisa) {
		FisaPontaj f = fise.getById(idFisa);
		List<Neconformitate> gasite = conformitate.verifica(f);
		f.inregistreazaVerificare(gasite);
		audit.inregistreaza(utilizator, "VERIFICARE_LEGALA", "FisaPontaj#" + idFisa,
				gasite.isEmpty() ? "conforma" : gasite.size() + " neconformitati");
		return gasite;
	}

	@Override
	public List<FisaPontaj> verificaToateFisele(String utilizator, Integer idSectie, LunaCalendaristica luna) {
		List<FisaPontaj> deVerificat = fise.findBySectieSiLuna(idSectie, luna).stream()
				.filter(f -> f.getStare() == StareFisa.GENERATA || f.getStare() == StareFisa.NECONFORMA).toList();
		deVerificat.forEach(f -> verificaFisa(utilizator, f.getIdFisa()));
		return deVerificat.stream().filter(f -> f.getStare() == StareFisa.NECONFORMA).toList();   // cele cu probleme
	}

	@Override
	public void aprobaFisa(String utilizator, Integer idFisa) {
		fise.getById(idFisa).aprobaHR();
		audit.inregistreaza(utilizator, "APROBARE_HR", "FisaPontaj#" + idFisa, "");
	}

	@Override
	public void respingeFisa(String utilizator, Integer idFisa, String motiv) {
		fise.getById(idFisa).respingeHR(motiv);
		audit.inregistreaza(utilizator, "RESPINGERE_HR", "FisaPontaj#" + idFisa, motiv);
	}

	@Override
	public BorderouSalarial genereazaBorderou(String utilizator, Integer idSectie, LunaCalendaristica luna,
			FormatExport format) {
		List<FisaPontaj> toate = fise.findBySectieSiLuna(idSectie, luna);
		long neaprobate = toate.stream().filter(f -> f.getStare() != StareFisa.APROBATA_HR).count();
		if (toate.isEmpty() || neaprobate > 0)
			throw new DomainException("Borderoul se genereaza cand toate fisele sectiei sunt aprobate HR ("
					+ neaprobate + " din " + toate.size() + " nu sunt).");
		BorderouSalarial b = borderouri.add(new BorderouSalarial(null, idSectie, luna, format));
		toate.forEach(b::adaugaFisa);
		audit.inregistreaza(utilizator, "BORDEROU_GENERAT", "BorderouSalarial#" + b.getIdBorderou(), toate.size() + " angajati");
		return b;
	}

	@Override
	public String exportaBorderou(String utilizator, Integer idBorderou) {
		BorderouSalarial b = borderouri.getById(idBorderou);
		String continut = export.exporta(b);
		if (b.getStare() == StareBorderou.IN_PREGATIRE)    // la al doilea export nu se mai schimba nimic (R5)
			b.marcheazaExportat(fise.findBySectieSiLuna(b.getIdSectie(), b.getLuna()));
		audit.inregistreaza(utilizator, "EXPORT_BORDEROU", "BorderouSalarial#" + idBorderou, export.numeFisier(b));
		return continut;
	}
}
