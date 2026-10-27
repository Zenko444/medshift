package org.medshift.application.pontaj;

import org.medshift.domain.pontaj.BorderouSalarial;
import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.FormatExport;
import org.medshift.domain.pontaj.Neconformitate;
import org.medshift.domain.shared.LunaCalendaristica;

import java.util.List;

/** Business Workflow (Use-case) Service pentru contextul Pontaj: UC3.1 - UC3.5. */
public interface IPontajWorkflowService {
	// UC3.1 + UC3.2: fisele se genereaza (si orele se segmenteaza) din graficul finalizat
	List<FisaPontaj> genereazaFise(String utilizator, Integer idGrafic);
	// UC3.3
	List<Neconformitate> verificaFisa(String utilizator, Integer idFisa);
	List<FisaPontaj> verificaToateFisele(String utilizator, Integer idSectie, LunaCalendaristica luna);
	// UC3.4
	void aprobaFisa(String utilizator, Integer idFisa);
	void respingeFisa(String utilizator, Integer idFisa, String motiv);
	// UC3.5
	BorderouSalarial genereazaBorderou(String utilizator, Integer idSectie, LunaCalendaristica luna, FormatExport format);
	String exportaBorderou(String utilizator, Integer idBorderou);
}
