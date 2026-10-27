package org.medshift.application.planificare;

import org.medshift.domain.planificare.GraficLunar;
import org.medshift.domain.planificare.MatriceAcoperire;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.LunaCalendaristica;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Business Workflow (Use-case) Service pentru contextul Planificare: UC1.1 - UC1.6 + CF2.3, CF2.4. */
public interface IPlanificareWorkflowService {
	// UC1.1 + UC1.2
	GraficLunar initializeazaGrafic(String utilizator, Integer idSectie, LunaCalendaristica luna,
			LocalDate termenDoleante, MatriceAcoperire matrice);
	// UC1.3: returneaza problemele ramase (lista goala = gata de publicare)
	List<String> genereazaGrafic(String utilizator, Integer idGrafic, String strategie, List<PersonalDisponibil> personal);
	Set<String> strategiiDisponibile();
	// UC1.4
	void publicaGrafic(String utilizator, Integer idGrafic);
	// UC1.5
	void confirmaTura(String utilizator, Integer idGrafic, Integer idTura, Integer idAngajat);
	int aplicaConfirmariTacite(Integer idGrafic);
	// CF2.4: concediu aprobat -> ture descoperite
	List<Tura> inregistreazaConcediu(String utilizator, Integer idGrafic, Integer idAngajat, LocalDate deLa, LocalDate panaLa);
	// CF2.3
	void schimbaTure(String utilizator, Integer idGrafic, Integer idTuraA, Integer idAngajatA, Integer idTuraB, Integer idAngajatB);
	// UC1.6
	void finalizeazaGrafic(String utilizator, Integer idGrafic);
}
