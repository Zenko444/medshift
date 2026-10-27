package org.medshift.services.personal;

import org.medshift.domain.personal.Concediu;
import org.medshift.domain.personal.DoleanteLunare;
import org.medshift.domain.planificare.PersonalDisponibil;
import org.medshift.domain.shared.LunaCalendaristica;
import org.medshift.domain.shared.Perioada;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class DisponibilitateServiceImpl implements IDisponibilitateService {

	private final IAngajatRepository angajati;
	private final IDoleanteLunareRepository doleante;
	private final IConcediuRepository concedii;

	public DisponibilitateServiceImpl(IAngajatRepository angajati, IDoleanteLunareRepository doleante,
			IConcediuRepository concedii) {
		this.angajati = angajati;
		this.doleante = doleante;
		this.concedii = concedii;
	}

	@Override
	public List<PersonalDisponibil> personalDisponibil(Integer idSectie, LunaCalendaristica luna) {
		List<LocalDate> zileLuna = luna.zile();
		Perioada perioadaLunii = new Perioada(zileLuna.get(0), luna.ultimaZi());
		List<Concediu> concediiAprobate = concedii.findAprobateInPerioada(perioadaLunii);

		return angajati.findBySectie(idSectie).stream()
				.filter(a -> zileLuna.stream().anyMatch(a::esteActivLa))      // are contract activ macar o zi
				.map(a -> {
					Set<LocalDate> indisponibil = new TreeSet<>();
					// 1. zilele fara contract activ
					zileLuna.stream().filter(zi -> !a.esteActivLa(zi)).forEach(indisponibil::add);
					// 2. concediile aprobate
					concediiAprobate.stream().filter(c -> c.getIdAngajat().equals(a.getIdAngajat()))
							.forEach(c -> zileLuna.stream().filter(c.getPerioada()::contine).forEach(indisponibil::add));
					// 3. doleantele: INDISPONIBIL e restrictie dura, PREFER_LIBER e doar preferinta
					Set<LocalDate> preferaLiber = Set.of();
					DoleanteLunare d = doleante.getByAngajatSiLuna(a.getIdAngajat(), luna).orElse(null);
					if (d != null) {
						indisponibil.addAll(d.zileIndisponibile());
						preferaLiber = d.zilePreferaLiber();
					}
					return new PersonalDisponibil(a.getIdAngajat(), a.getGrad(), indisponibil, preferaLiber);
				})
				.toList();
	}
}
