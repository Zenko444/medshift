package org.medshift.domain.planificare;

import org.medshift.domain.shared.*;

public enum StareAsignare {
	PROPUSA,                  // inainte de publicare
	IN_ASTEPTARE_CONFIRMARE,  // dupa publicare, fereastra de 3 zile
	CONFIRMATA,
	CONTESTATA,
	ANULATA;

	public boolean esteActiva() {
		return this != ANULATA;
	}
}
