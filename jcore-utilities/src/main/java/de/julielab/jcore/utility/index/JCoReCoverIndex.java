/** 
 * 
 * Copyright (c) 2017, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: 
 * 
 * Description:
 **/
package de.julielab.jcore.utility.index;

import de.julielab.jcore.utility.JCoReTools;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * <p>
 * Use when: You need access to annotations between a given begin- and
 * end-offset or completely covered by another annotation.
 * </p>
 * 
 * Allows efficient access to annotation which are covered completely by a given
 * begin - end offset span.
 * 
 * @author faessler
 *
 * @param <E>
 */
public class JCoReCoverIndex<E extends Annotation> implements JCoReAnnotationIndex<E> {
	private List<E> index;
	private boolean frozen;

	public JCoReCoverIndex() {
		index = new ArrayList<>();
	}

	public JCoReCoverIndex(JCas jcas, int type) {
		this(jcas, jcas.getCasType(type));
	}

	public JCoReCoverIndex(JCas jcas, Type type) {
		this();
		index(jcas, type);
		freeze();
	}

	/**
	 * Freeze the index to allow searching it. The index can only be searched if
	 * frozen.
	 */
	public void freeze() {
		frozen = true;
		Collections.sort(index, Comparators.beginOffsetComparator());
	}

	/**
	 * Un-freeze the index to allow new elements to be added.
	 */
	public void melt() {
		frozen = false;
	}

	/**
	 * Adds the whole contents of the CAS annotation index of type <tt>type</tt>
	 * into the index data structure. To access the indexed annotations, first
	 * {@link #freeze()} the index and then {@link #search(int, int)} it.
	 * 
	 * @param jCas
	 *            A CAS instance.
	 * @param type
	 *            The annotation type to index.
	 */
	public void index(JCas jCas, int type) {
		index(jCas, jCas.getCasType(type));
	}

	/**
	 * Adds the whole contents of the CAS annotation index of type <tt>type</tt>
	 * into the index data structure. To access the indexed annotations, first
	 * {@link #freeze()} the index and then {@link #search(int, int)} it.
	 * 
	 * @param jCas
	 *            A CAS instance.
	 * @param type
	 *            The annotation type to index.
	 */
	@SuppressWarnings("unchecked")
	public void index(JCas jCas, Type type) {
		FSIterator<Annotation> it = jCas.getAnnotationIndex(type).iterator();
		while (it.hasNext()) {
			Annotation annotation = (Annotation) it.next();
			index((E) annotation);
		}
	}

	/**
	 * Adds <tt>annotation</tt> into the index data structure. To access the
	 * indexed annotations, first {@link #freeze()} the index and then
	 * {@link #search(int, int)} it.
	 * 
	 * @param jCas
	 *            A CAS instance.
	 * @param type
	 *            The annotation type to index.
	 */
	public void index(E annotation) {
		if (frozen)
			throw new IllegalStateException("This index is frozen and cannot except further items.");
		index.add(annotation);
	}

	/**
	 * Returns all annotations in this index that are completely covered by the
	 * annotation <tt>a</tt>.
	 * 
	 * @param a
	 *            The annotation for which contained annotations should be
	 *            returned.
	 * @return Indexed annotations whose offsets lie between the offsets of
	 *         <tt>a</tt>, including the exact begin and end offsets.
	 */
	public Stream<E> search(Annotation a) {
		return search(a.getBegin(), a.getEnd());
	}

	/**
	 * Returns all annotations in this index that are completely covered by the
	 * span given by <tt>begin</tt> and <tt>end</tt>.
	 * 
	 * @param begin
	 *            The lowest offset where returned annotations may begin.
	 * @param end
	 *            The largest offset where returned annotations may end.
	 * @return Indexed annotations whose offsets lie between <tt>begin</tt> and
	 *         <tt>end</tt>, inclusive.
	 */
	public Stream<E> search(int begin, int end) {
		if (!frozen)
			throw new IllegalStateException(
					"This index is not frozen and cannot be used yet. Freeze the index before searching.");
		if (index.isEmpty())
			return Stream.empty();
		int lowerIndex = insertionPoint(JCoReTools.binarySearch(index, a -> a.getBegin(), begin));
		int upperIndex = insertionPoint(JCoReTools.binarySearch(index, a -> a.getBegin(), end));
		return index.subList(lowerIndex, upperIndex).stream().filter(a -> a.getEnd() <= end);
	}

	private int insertionPoint(int i) {
		return i < 0 ? -(i + 1) : i;
	}

	@Override
	public void add(E a) {
		index(a);
	}
}
