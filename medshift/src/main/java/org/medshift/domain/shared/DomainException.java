package org.medshift.domain.shared;

/** Aruncata cand o operatie ar incalca o regula de business (invariant). */
public class DomainException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DomainException(String message) {
		super(message);
	}
}
