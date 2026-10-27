package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.SegmentOre;
import org.medshift.domain.shared.IntervalOrar;
import org.medshift.domain.shared.TipTura;

import java.util.List;

/** Computation Service (CF3.2): imparte orele unei ture pe categorii (regula C4). */
public interface ISegmentareOreService {
	List<SegmentOre> segmenteaza(IntervalOrar interval, TipTura tipTura);
}
