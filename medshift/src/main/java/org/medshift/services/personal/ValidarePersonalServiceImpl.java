package org.medshift.services.personal;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.medshift.domain.personal.Angajat;
import org.medshift.domain.personal.Concediu;
import org.medshift.domain.personal.DoleanteLunare;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Domain Validation Service pentru contextul Personal: validare JavaBean + reguli care cer acces la repository.
@Service
public class ValidarePersonalServiceImpl implements IValidarePersonalService {

	private final Validator validator;
	private final IAngajatRepository angajati;
	private final IConcediuRepository concedii;

	public ValidarePersonalServiceImpl(Validator validator, IAngajatRepository angajati, IConcediuRepository concedii) {
		this.validator = validator;
		this.angajati = angajati;
		this.concedii = concedii;
	}

	@Override
	public List<String> valideazaCampuri(Object entitate) {
		List<String> erori = new ArrayList<>();
		for (ConstraintViolation<Object> v : validator.validate(entitate))
			erori.add(v.getPropertyPath() + ": " + v.getMessage());
		return erori;
	}

	@Override
	public List<String> valideazaAngajat(Angajat a) {
		List<String> erori = valideazaCampuri(a);
		if (a.getEmail() != null)
			angajati.findByEmail(a.getEmail()).filter(existent -> !existent.equals(a))
					.ifPresent(existent -> erori.add("email: adresa " + a.getEmail() + " este deja folosita (V1)."));
		return erori;
	}

	@Override
	public List<String> valideazaConcediu(Concediu c) {
		List<String> erori = valideazaCampuri(c);
		if (c.getIdAngajat() != null && angajati.findById(c.getIdAngajat()).isEmpty())
			erori.add("idAngajat: angajatul " + c.getIdAngajat() + " nu exista.");
		if (c.getIdAngajat() != null && c.getPerioada() != null)
			concedii.findByAngajat(c.getIdAngajat()).stream()
					.filter(altul -> !altul.equals(c) && altul.seSuprapuneCu(c))
					.forEach(altul -> erori.add("perioada: se suprapune cu concediul " + altul.getIdConcediu()
							+ " (" + altul.getPerioada() + ") - regula A9."));
		return erori;
	}

	@Override
	public List<String> valideazaDoleante(DoleanteLunare d, LocalDate termenDoleante, LocalDateTime acum) {
		List<String> erori = valideazaCampuri(d);
		if (d.getIdAngajat() != null && angajati.findById(d.getIdAngajat()).isEmpty())
			erori.add("idAngajat: angajatul " + d.getIdAngajat() + " nu exista.");
		if (acum.toLocalDate().isAfter(termenDoleante))
			erori.add("termen: termenul de depunere (" + termenDoleante + ") a expirat (WF1).");
		return erori;
	}
}
