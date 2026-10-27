package org.medshift.services.shared;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contractul comun al tuturor repository-urilor (cate unul pentru fiecare Aggregate Root).
 * Aceleasi operatii ca IProjectEntityRepository din T2.C3, dar generice: T = tipul agregatului.
 */
public interface IEntityRepository<T> {
	Integer getNextID();
	//
	T getById(Integer id);                          // arunca DomainException daca nu exista
	Optional<T> findById(Integer id);               // varianta "cutie care poate fi goala"
	T get(T sample);                                // query by example (ca get(Project sample) din T2.C3)
	List<T> findByExample(Predicate<T> criteriu);   // cautare dupa orice conditie
	Collection<T> toCollection();                   // getAll
	//
	T add(T entity);
	Collection<T> addAll(Collection<T> entities);
	boolean remove(T entity);
	boolean removeAll(Collection<T> entities);
	//
	int size();
}
