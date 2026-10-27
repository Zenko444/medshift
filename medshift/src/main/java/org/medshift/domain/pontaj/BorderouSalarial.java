package org.medshift.domain.pontaj;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.LunaCalendaristica;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.*;

/** Aggregate Root (Pontaj): borderoul lunar al unei sectii, exportat catre salarizare (CF3.5). */
public class BorderouSalarial implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer idBorderou;
	@NotNull private Integer idSectie;
	@NotNull private LunaCalendaristica luna;
	@NotNull private FormatExport format = FormatExport.CSV;
	@NotNull private StareBorderou stare = StareBorderou.IN_PREGATIRE;
	@Valid private List<LinieBorderou> linii = new ArrayList<>();

	public BorderouSalarial() {
	}

	public BorderouSalarial(Integer idBorderou, Integer idSectie, LunaCalendaristica luna, FormatExport format) {
		this.idBorderou = idBorderou;
		this.idSectie = Objects.requireNonNull(idSectie, "idSectie");
		this.luna = Objects.requireNonNull(luna, "luna");
		this.format = Objects.requireNonNull(format, "format");
	}

	/** Regula A10: intra in borderou doar fisele aprobate HR, din aceeasi luna, cate una pe angajat. */
	public void adaugaFisa(FisaPontaj fisa) {
		if (stare != StareBorderou.IN_PREGATIRE)
			throw new DomainException("Borderoul a fost deja exportat.");
		if (fisa.getStare() != StareFisa.APROBATA_HR)
			throw new DomainException("Fisa " + fisa.getIdFisa() + " nu este aprobata de HR.");
		if (!fisa.getIdSectie().equals(idSectie))
			throw new DomainException("Fisa este din sectia " + fisa.getIdSectie() + ", borderoul pentru sectia " + idSectie);
		if (!fisa.getLuna().equals(luna))
			throw new DomainException("Fisa este pentru luna " + fisa.getLuna() + ", borderoul pentru " + luna);
		if (linii.stream().anyMatch(l -> l.idAngajat().equals(fisa.getIdAngajat())))
			throw new DomainException("Angajatul " + fisa.getIdAngajat() + " este deja in borderou.");
		linii.add(new LinieBorderou(fisa.getIdAngajat(), fisa.getIdFisa(), fisa.totaluri()));
	}

	/** Marcheaza borderoul si fisele incluse ca exportate. Fisele se dau ca parametru (referinte prin ID). */
	public void marcheazaExportat(Collection<FisaPontaj> fiseIncluse) {
		if (stare != StareBorderou.IN_PREGATIRE)
			throw new DomainException("Borderoul a fost deja exportat.");
		if (linii.isEmpty())
			throw new DomainException("Un borderou gol nu poate fi exportat.");
		fiseIncluse.forEach(FisaPontaj::marcheazaExportata);
		stare = StareBorderou.EXPORTAT;
	}

	public TotalOre totalSectie() {
		return linii.stream().map(LinieBorderou::totaluri).reduce(TotalOre.ZERO, TotalOre::plus);
	}

	/* ---- getteri ---- */
	public Integer getIdBorderou() { return idBorderou; }
	public Integer getIdSectie() { return idSectie; }
	public LunaCalendaristica getLuna() { return luna; }
	public FormatExport getFormat() { return format; }
	public StareBorderou getStare() { return stare; }
	public List<LinieBorderou> getLinii() { return Collections.unmodifiableList(linii); }

	public void setIdBorderou(Integer idBorderou) {
		if (this.idBorderou != null)
			throw new DomainException("ID-ul nu se mai poate schimba.");
		this.idBorderou = idBorderou;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof BorderouSalarial b && idBorderou != null && idBorderou.equals(b.idBorderou);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(idBorderou);
	}

	@Override
	public String toString() {
		return "\nBorderouSalarial [id=" + idBorderou + ", sectia " + idSectie + ", luna=" + luna + ", " + stare
				+ ", linii=" + linii.size() + "]";
	}
}
