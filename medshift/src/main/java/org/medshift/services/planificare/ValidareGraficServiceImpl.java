package org.medshift.services.planificare;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.medshift.domain.planificare.GraficLunar;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Domain Validation Service: combina validarea JavaBean (Validator) cu invariantii agregatului.
@Service
public class ValidareGraficServiceImpl implements IValidareGraficService {

	private final Validator validator;   // furnizat automat de Spring Boot (spring-boot-starter-validation)

	public ValidareGraficServiceImpl(Validator validator) {
		this.validator = validator;
	}

	@Override
	public List<String> valideazaCampuri(GraficLunar grafic) {
		List<String> erori = new ArrayList<>();
		for (ConstraintViolation<GraficLunar> v : validator.validate(grafic))
			erori.add(v.getPropertyPath() + ": " + v.getMessage());
		return erori;
	}

	@Override
	public List<String> valideazaRestrictii(GraficLunar grafic) {
		return grafic.verificaRestrictii();
	}

	@Override
	public List<String> valideaza(GraficLunar grafic) {
		List<String> toate = new ArrayList<>(valideazaCampuri(grafic));
		toate.addAll(valideazaRestrictii(grafic));
		return toate;
	}

	@Override
	public boolean poateFiPublicat(GraficLunar grafic) {
		return valideaza(grafic).isEmpty();
	}
}
