package org.medshift.application.personal;

import org.medshift.domain.personal.*;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.planificare.Tura;
import org.medshift.domain.shared.GradMedical;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.Perioada;

import java.util.List;

/** Business Workflow (Use-case) Service pentru contextul Personal: UC2.1 - UC2.4. */
public interface IPersonalWorkflowService {
	// UC2.1
	Angajat inregistreazaAngajat(String utilizator, String nume, String prenume, String email, GradMedical grad,
			String specializare, Integer idSectie, Contract contract);
	// UC2.2: termenul se ia din graficul sectiei pentru luna respectiva
	DoleanteLunare depuneDoleanta(String utilizator, Integer idAngajat, LunaCalendaristica luna, PreferintaZi preferinta);
	// legatura cu Planificarea: lista folosita la generarea graficului
	List<PersonalDisponibil> pregatesteDisponibilitati(Integer idSectie, LunaCalendaristica luna);
	// UC2.4: urgenta medicala e aprobata automat si marcheaza imediat turele descoperite
	Concediu solicitaConcediu(String utilizator, Integer idAngajat, TipConcediu tip, Perioada perioada);
	List<Tura> aprobaConcediu(String utilizator, Integer idConcediu);
	void respingeConcediu(String utilizator, Integer idConcediu, String motiv);
	// UC2.3: schimb de tura Peer-to-Peer
	CerereSchimbTura cereSchimbTura(String utilizator, Integer idGrafic, Integer idSolicitant, Integer idTuraOferita,
			Integer idColeg, Integer idTuraSolicitata);
	CerereSchimbTura raspundeColegul(String utilizator, Integer idCerere, boolean accepta, String motiv);
	CerereSchimbTura aprobaSchimbDeSef(String utilizator, Integer idCerere);
}
