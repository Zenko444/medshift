package org.medshift.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Ceas pe care testele il pot muta (ex.: "suntem la sfarsitul lunii"), ca regulile de timp sa fie testabile. */
public class CeasControlabil extends Clock {
	private Instant acum;
	private final ZoneId zona;

	public CeasControlabil(LocalDateTime start, ZoneId zona) {
		this.zona = zona;
		seteaza(start);
	}

	public void seteaza(LocalDateTime moment) {
		this.acum = moment.atZone(zona).toInstant();
	}

	@Override public ZoneId getZone() { return zona; }
	@Override public Clock withZone(ZoneId zone) { return new CeasControlabil(LocalDateTime.ofInstant(acum, zone), zone); }
	@Override public Instant instant() { return acum; }
}
