package org.medshift.services.shared;

import org.medshift.domain.shared.DomainException;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Implementarea comuna, in memorie (Map = new HashMap), a unui repository.
 * Fiecare repository concret spune doar cum se citeste si cum se seteaza ID-ul agregatului lui.
 * Intr-un sprint urmator, aceste clase vor fi inlocuite cu repository-uri JPA (baza de date).
 */
public abstract class RepositoryInMemorie<T> implements IEntityRepository<T> {
	private static final Logger logger = Logger.getLogger(RepositoryInMemorie.class.getName());

	protected final Map<Integer, T> entities = new HashMap<>();
	private Integer nextID = 0;

	protected RepositoryInMemorie() {
		logger.info(">>> BEAN: " + getClass().getSimpleName() + " instantiated!");
	}

	/** Cum aflu ID-ul unui agregat (ex.: angajat.getIdAngajat()). */
	protected abstract Integer idOf(T entity);

	/** Cum ii dau un ID nou unui agregat care nu are inca unul. */
	protected abstract void assignId(T entity, Integer id);

	/** Numele afisat in mesajele de eroare (ex.: "Angajatul"). */
	protected abstract String numeEntitate();

	@Override
	public synchronized Integer getNextID() {
		return ++nextID;
	}

	@Override
	public T getById(Integer id) {
		return findById(id).orElseThrow(() -> new DomainException(numeEntitate() + " cu ID " + id + " nu exista."));
	}

	@Override
	public Optional<T> findById(Integer id) {
		return Optional.ofNullable(entities.get(id));
	}

	/**
	 * Query by example, ca in T2.C3: primesc un obiect-model si intorc obiectul stocat care ii corespunde.
	 * Corespondenta se face dupa identitate (ID), adica dupa equals() al entitatii.
	 */
	@Override
	public T get(T sample) {
		if (sample == null || idOf(sample) == null)
			return null;
		return entities.get(idOf(sample));
	}

	@Override
	public List<T> findByExample(Predicate<T> criteriu) {
		return entities.values().stream().filter(criteriu).toList();
	}

	@Override
	public Collection<T> toCollection() {
		return new ArrayList<>(entities.values());
	}

	@Override
	public T add(T entity) {
		if (idOf(entity) == null)
			assignId(entity, getNextID());
		verificaUnicitate(entity);
		entities.put(idOf(entity), entity);
		return entity;
	}

	/** Regulile de unicitate ale fiecarui agregat (ex.: un singur grafic pe sectie si luna). Implicit: niciuna. */
	protected void verificaUnicitate(T entity) {
	}

	@Override
	public Collection<T> addAll(Collection<T> entities) {
		for (T e : entities)
			add(e);
		return entities;
	}

	@Override
	public boolean remove(T entity) {
		return entity != null && idOf(entity) != null && entities.remove(idOf(entity)) != null;
	}

	@Override
	public boolean removeAll(Collection<T> entities) {
		boolean toate = true;
		for (T e : entities)
			if (!remove(e))
				toate = false;
		return toate;
	}

	@Override
	public int size() {
		return entities.size();
	}
}
