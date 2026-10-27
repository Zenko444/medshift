package org.medshift.domain.personal;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Aggregate Root (Personal): doleantele unui angajat pentru o luna (CF2.2). */
public class DoleanteLunare implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer idDoleante;
	@NotNull(message = "Doleantele trebuie sa apartina unui angajat.")
	private Integer idAngajat;
	@NotNull(message = "Luna este obligatorie.")
	private LunaCalendaristica luna;
	private LocalDateTime dataUltimeiModificari;
	@Valid
	private List<PreferintaZi> preferinte = new ArrayList<>();

	public DoleanteLunare() {
	}

	public DoleanteLunare(Integer idDoleante, Integer idAngajat, LunaCalendaristica luna) {
		this.idDoleante = idDoleante;
		this.idAngajat = Objects.requireNonNull(idAngajat, "idAngajat");
		this.luna = Objects.requireNonNull(luna, "luna");
	}

	/** Adauga sau inlocuieste doleanta pentru o zi; permis doar pana la termen (WF1) si doar in luna tinta (V9). */
	public void adaugaPreferinta(PreferintaZi p, LocalDate termenDoleante, LocalDateTime acum) {
		verificaTermen(termenDoleante, acum);
		if (!luna.contine(p.data()))
			throw new DomainException("Data " + p.data() + " nu apartine lunii " + luna);
		preferinte.removeIf(x -> x.data().equals(p.data()));      // o singura doleanta pe zi
		preferinte.add(p);
		dataUltimeiModificari = acum;
	}

	public void retrageaPreferinta(LocalDate data, LocalDate termenDoleante, LocalDateTime acum) {
		verificaTermen(termenDoleante, acum);
		preferinte.removeIf(x -> x.data().equals(data));
		dataUltimeiModificari = acum;
	}

	private void verificaTermen(LocalDate termenDoleante, LocalDateTime acum) {
		if (acum.toLocalDate().isAfter(termenDoleante))
			throw new DomainException("Termenul de depunere a doleantelor (" + termenDoleante + ") a expirat.");
	}

	public Set<LocalDate> zileCu(TipPreferinta tip) {
		return preferinte.stream().filter(p -> p.tip() == tip).map(PreferintaZi::data)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public Set<LocalDate> zileIndisponibile() {
		return zileCu(TipPreferinta.INDISPONIBIL);
	}

	public Set<LocalDate> zilePreferaLiber() {
		return zileCu(TipPreferinta.PREFER_LIBER);
	}

	/* ---- getteri ---- */
	public Integer getIdDoleante() { return idDoleante; }
	public Integer getIdAngajat() { return idAngajat; }
	public LunaCalendaristica getLuna() { return luna; }
	public LocalDateTime getDataUltimeiModificari() { return dataUltimeiModificari; }
	public List<PreferintaZi> getPreferinte() { return Collections.unmodifiableList(preferinte); }

	public void setIdDoleante(Integer idDoleante) {
		if (this.idDoleante != null)
			throw new DomainException("ID-ul nu se mai poate schimba.");
		this.idDoleante = idDoleante;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof DoleanteLunare d && idDoleante != null && idDoleante.equals(d.idDoleante);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idDoleante);
	}

	@Override
	public String toString() {
		return "\nDoleanteLunare [id=" + idDoleante + ", angajat=" + idAngajat + ", luna=" + luna
				+ ", preferinte=" + preferinte.size() + "]";
	}
}
