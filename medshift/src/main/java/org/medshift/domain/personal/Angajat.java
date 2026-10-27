package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.GradMedical;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Aggregate Root (Personal): profilul profesional al unui membru al personalului medical (CF2.1). */
public class Angajat implements Serializable, Comparable<Angajat> {
	private static final long serialVersionUID = 1L;

	private Integer idAngajat;
	@NotNull(message = "Numele este obligatoriu.") @Valid
	private NumeComplet nume;
	@NotNull(message = "Emailul este obligatoriu.") @Valid
	private Email email;
	@NotNull(message = "Gradul medical este obligatoriu.")
	private GradMedical grad;
	@Size(max = 80, message = "Specializarea are maxim 80 de caractere.")
	private String specializare;
	@NotNull(message = "Angajatul trebuie sa apartina unei sectii.")
	private Integer idSectie;                 // referinta prin identitate
	@NotNull(message = "Angajatul trebuie sa aiba un contract.") @Valid
	private Contract contract;
	@Valid
	private Set<Competenta> competente = new LinkedHashSet<>();

	public Angajat() {
	}

	public Angajat(Integer idAngajat, NumeComplet nume, Email email, GradMedical grad, String specializare,
			Integer idSectie, Contract contract) {
		this.idAngajat = idAngajat;
		this.nume = Objects.requireNonNull(nume, "nume");
		this.email = Objects.requireNonNull(email, "email");
		this.grad = Objects.requireNonNull(grad, "grad");
		this.specializare = specializare;
		this.idSectie = Objects.requireNonNull(idSectie, "idSectie");
		this.contract = Objects.requireNonNull(contract, "contract");
	}

	/* ---- comportament ---- */

	public void adaugaCompetenta(Competenta c) {
		competente.add(Objects.requireNonNull(c));
	}

	public void eliminaCompetenta(String cod) {
		competente.removeIf(c -> c.cod().equalsIgnoreCase(cod));
	}

	public boolean areCompetenta(String cod) {
		return competente.stream().anyMatch(c -> c.cod().equalsIgnoreCase(cod));
	}

	/** Un contract nou il inlocuieste pe cel vechi (ex.: trecere de la norma partiala la norma intreaga). */
	public void schimbaContract(Contract nou) {
		this.contract = Objects.requireNonNull(nou, "contract");
	}

	public void schimbaEmail(Email nou) {
		this.email = Objects.requireNonNull(nou, "email");
	}

	public void mutaInSectia(Integer idSectieNoua) {
		this.idSectie = Objects.requireNonNull(idSectieNoua, "idSectie");
	}

	public boolean esteActivLa(LocalDate data) {
		return contract != null && contract.esteActivLa(data);
	}

	/** Poate fi pus pe o tura care cere acest grad si aceste competente? (regula A5) */
	public boolean esteEligibilPentru(GradMedical gradCerut, Set<String> competenteCerute, LocalDate data) {
		return grad == gradCerut && esteActivLa(data) && competenteCerute.stream().allMatch(this::areCompetenta);
	}

	/* ---- getteri ---- */
	public Integer getIdAngajat() { return idAngajat; }
	public NumeComplet getNume() { return nume; }
	public Email getEmail() { return email; }
	public GradMedical getGrad() { return grad; }
	public String getSpecializare() { return specializare; }
	public Integer getIdSectie() { return idSectie; }
	public Contract getContract() { return contract; }
	public Set<Competenta> getCompetente() { return Collections.unmodifiableSet(competente); }

	/** Folosit de repository cand angajatul nu are inca ID. */
	public void setIdAngajat(Integer idAngajat) {
		if (this.idAngajat != null)
			throw new DomainException("ID-ul angajatului nu se mai poate schimba.");
		this.idAngajat = idAngajat;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Angajat a && idAngajat != null && idAngajat.equals(a.idAngajat);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idAngajat);
	}

	@Override
	public int compareTo(Angajat alt) {
		return idAngajat.compareTo(alt.idAngajat);
	}

	@Override
	public String toString() {
		return "\nAngajat [id=" + idAngajat + ", " + nume + ", " + grad + ", sectia " + idSectie + ", " + contract + "]";
	}
}
