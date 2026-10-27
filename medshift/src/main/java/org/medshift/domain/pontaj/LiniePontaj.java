package org.medshift.domain.pontaj;

import org.medshift.domain.shared.DomainException;
import org.medshift.domain.shared.IntervalOrar;
import org.medshift.domain.shared.TipTura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Entitate in agregatul FisaPontaj: o tura efectiv lucrata, cu orele impartite pe categorii. */
public class LiniePontaj implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull private Integer idTura;          // referinta catre tura din GraficLunar
	@NotNull private LocalDate data;
	@NotNull private TipTura tipTura;
	@NotNull private IntervalOrar interval;
	@Valid private List<SegmentOre> segmente;

	public LiniePontaj() {
	}

	LiniePontaj(Integer idTura, LocalDate data, TipTura tipTura, IntervalOrar interval, List<SegmentOre> segmente) {
		this.idTura = Objects.requireNonNull(idTura, "idTura");
		this.data = Objects.requireNonNull(data, "data");
		this.tipTura = Objects.requireNonNull(tipTura, "tipTura");
		this.interval = Objects.requireNonNull(interval, "interval");
		this.segmente = List.copyOf(segmente);
		double ore = this.segmente.stream().mapToDouble(SegmentOre::ore).sum();
		if (Math.abs(ore - interval.durataOre()) > 0.01)        // regula V10: segmentele acopera exact tura
			throw new DomainException("Segmentele (" + ore + " h) nu corespund duratei turei (" + interval.durataOre() + " h).");
	}

	public TotalOre totaluri() {
		return TotalOre.din(segmente);
	}

	public Integer getIdTura() { return idTura; }
	public LocalDate getData() { return data; }
	public TipTura getTipTura() { return tipTura; }
	public IntervalOrar getInterval() { return interval; }
	public List<SegmentOre> getSegmente() { return Collections.unmodifiableList(segmente); }

	@Override
	public String toString() {
		return "\n\tLiniePontaj [tura=" + idTura + ", " + data + ", " + tipTura + ", " + segmente + "]";
	}
}
