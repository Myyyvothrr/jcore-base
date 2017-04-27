package de.julielab.jcore.utility.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * <p>
 * This class builds a map from arbitrary keys to collections of annotations.
 * For convenience access, class takes suppliers for the generation of index or
 * search terms as well as suppliers for the actual collection implementations
 * that should be used within the index and for search results. Thus, it's just
 * a kind of convenience framework around a map.
 * </p>
 * 
 * @author faessler
 *
 * @param <T>
 *            The annotation type the index is over.
 * @param <K>
 *            The key type used to index the annotations.
 * @param <C>
 *            The collection type (e.g. ArrayList<Token>) used to store
 *            annotations in the index.
 * @param <U>
 *            The collection type (e.g. TreeSet<Sentence>) used to return search
 *            results.
 */
public class JCoReMapAnnotationIndex<K extends Comparable<K>, T extends Annotation>  implements JCoReAnnotationIndex<T> {

	protected final Map<K, Collection<T>> index;
	protected final IndexTermGenerator<K> indexTermGenerator;
	protected final IndexTermGenerator<K> searchTermGenerator;
	protected Supplier<Collection<T>> indexAnnotationStorageSupplier;

	/**
	 * This is the full constructor of the map index. It takes parameters for
	 * virtually every aspect of the index. For a quicker start, you might want
	 * to refer to one of its subclasses, e.g.
	 * {@link JCoReHashMapAnnotationIndex}. Using the constructor immediately
	 * build the index from the given <tt>jCas</tt> for the annotation type
	 * given by <tt>type</tt>.
	 * 
	 * @param indexMapSupplier
	 *            A supplier for the map that should be used as the index.
	 * @param indexTermGenerator
	 *            Generates index terms of generic parameter type K. Those index
	 *            terms will be extracted from indexed annotations.
	 * @param searchTermGenerator
	 *            Generates search terms of generic parameter K. The index will
	 *            extract all {@link IndexEntry} items in the index matching one
	 *            of the generated terms. This may be the very same term
	 *            generator passed for indexTermGenerator.
	 * @param indexAnnotationCollectionSupplier
	 *            A supplier for the collection data structure used to store
	 *            annotations in the index. In case of single index hits during
	 *            a search, this data structure is returned directly to save
	 *            time, if it is compatible with the return type specified by
	 *            the generic type parameter U (search result return type). This
	 *            way the desired output structure can be specified (e.g. a
	 *            TreeSet with a specific comparator).
	 * @param resultCollectionSupplier
	 *            In case a multiple search terms for a search as generated by
	 *            searchTermGenerator, not the index annotation collection is
	 *            returned but this supplier is used to create a new collection
	 *            to return search results for all search terms. Thus, when it
	 *            is expected that searchTermGenerator will often generate
	 *            multiple search terms, the indexAnnotationCollectionSupplier
	 *            should create a collection efficient for adding and iterating
	 *            and the resultCollectionSupplier should create a collection
	 *            reflects the desired output format.
	 * @param jCas
	 *            A JCas containing annotations that should be indexed.
	 * @param type
	 *            The UIMA type system type, belonging to <tt>jCas</tt>, that
	 *            should be indexed.
	 */
	public JCoReMapAnnotationIndex(Supplier<Map<K, Collection<T>>> indexMapSupplier,
			IndexTermGenerator<K> indexTermGenerator, IndexTermGenerator<K> searchTermGenerator, JCas jCas, Type type) {
		this.indexTermGenerator = indexTermGenerator;
		this.searchTermGenerator = searchTermGenerator;
		this.index = indexMapSupplier.get();
		indexAnnotationStorageSupplier = ArrayList::new;
		if (jCas != null && type != null)
			index(jCas, type);
	}

	/**
	 * This is the full constructor of the map index. It takes parameters for
	 * virtually every aspect of the index. For a quicker start, you might want
	 * to refer to one of its subclasses, e.g.
	 * {@link JCoReHashMapAnnotationIndex}. Using the constructor immediately
	 * build the index from the given <tt>jCas</tt> for the annotation type
	 * given by <tt>type</tt>.
	 * 
	 * @param indexMapSupplier
	 *            A supplier for the map that should be used as the index.
	 * @param indexTermGenerator
	 *            Generates index terms of generic parameter type K. Those index
	 *            terms will be extracted from indexed annotations.
	 * @param searchTermGenerator
	 *            Generates search terms of generic parameter K. The index will
	 *            extract all {@link IndexEntry} items in the index matching one
	 *            of the generated terms. This may be the very same term
	 *            generator passed for indexTermGenerator.
	 * @param indexAnnotationCollectionSupplier
	 *            A supplier for the collection data structure used to store
	 *            annotations in the index. In case of single index hits during
	 *            a search, this data structure is returned directly to save
	 *            time, if it is compatible with the return type specified by
	 *            the generic type parameter U (search result return type). This
	 *            way the desired output structure can be specified (e.g. a
	 *            TreeSet with a specific comparator).
	 * @param resultCollectionSupplier
	 *            In case a multiple search terms for a search as generated by
	 *            searchTermGenerator, not the index annotation collection is
	 *            returned but this supplier is used to create a new collection
	 *            to return search results for all search terms. Thus, when it
	 *            is expected that searchTermGenerator will often generate
	 *            multiple search terms, the indexAnnotationCollectionSupplier
	 *            should create a collection efficient for adding and iterating
	 *            and the resultCollectionSupplier should create a collection
	 *            reflects the desired output format.
	 * @param jCas
	 *            A JCas containing annotations that should be indexed.
	 * @param type
	 *            The UIMA type system type, belonging to <tt>jCas</tt>, that
	 *            should be indexed.
	 */
	public JCoReMapAnnotationIndex(Supplier<Map<K, Collection<T>>> indexMapSupplier,
			IndexTermGenerator<K> indexTermGenerator, IndexTermGenerator<K> searchTermGenerator, JCas jCas, int type) {
		this(indexMapSupplier, indexTermGenerator, searchTermGenerator, jCas, jCas.getCasType(type));
	}

	/**
	 * 
	 * @param indexMapSupplier
	 *            A supplier for the map that should be used as the index.
	 * @param indexTermGenerator
	 *            Generates index terms of generic parameter type K. Those index
	 *            terms will be extracted from indexed annotations.
	 * @param searchTermGenerator
	 *            Generates search terms of generic parameter K. The index will
	 *            extract all {@link IndexEntry} items in the index matching one
	 *            of the generated terms. This may be the very same term
	 *            generator passed for indexTermGenerator.
	 * @param indexAnnotationCollectionSupplier
	 *            A supplier for the collection data structure used to store
	 *            annotations in the index. In case of single index hits during
	 *            a search, this data structure is returned directly to save
	 *            time, if it is compatible with the return type specified by
	 *            the generic type parameter U (search result return type). This
	 *            way the desired output structure can be specified (e.g. a
	 *            TreeSet with a specific comparator).
	 * @param resultCollectionSupplier
	 *            In case a multiple search terms for a search as generated by
	 *            searchTermGenerator, not the index annotation collection is
	 *            returned but this supplier is used to create a new collection
	 *            to return search results for all search terms. Thus, when it
	 *            is expected that searchTermGenerator will often generate
	 *            multiple search terms, the indexAnnotationCollectionSupplier
	 *            should create a collection efficient for adding and iterating
	 *            and the resultCollectionSupplier should create a collection
	 *            reflects the desired output format.
	 */
	public JCoReMapAnnotationIndex(Supplier<Map<K, Collection<T>>> indexMapSupplier,
			IndexTermGenerator<K> indexTermGenerator, IndexTermGenerator<K> searchTermGenerator) {
		this(indexMapSupplier, indexTermGenerator, searchTermGenerator, null, null);
	}

	/**
	 * Indexes the whole contents of the CAS annotation index of type
	 * <tt>type</tt>. For each annotation, the {@link #indexTermGenerator} is
	 * used to create terms with which the annotation will be associated in the
	 * index and can be retrieved by a <code>search</code> method.
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
	 * Indexes the whole contents of the CAS annotation index of type
	 * <tt>type</tt>. For each annotation, the {@link #indexTermGenerator} is
	 * used to create terms with which the annotation will be associated in the
	 * index and can be retrieved by a <code>search</code> method.
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
			index((T) annotation);
		}
	}

	/**
	 * Indexes the annotation <tt>a</tt>. The {@link #indexTermGenerator} is
	 * used to create terms with which the annotation will be associated in the
	 * index and can be retrieved by a <code>search</code> method.
	 * 
	 * @param a
	 *            The annotation to index.
	 */
	@SuppressWarnings("unchecked")
	public void index(T a) {
		Object o = indexTermGenerator.generateIndexTerms(a);
		if (o instanceof Stream) {
			Stream<K> indexTerms = (Stream<K>) o;
			indexTerms.forEach(t -> {
				index(t, a);
			});
			indexTerms.close();
		} else {
			index((K) o, a);
		}
	}

	private void index(K t, T a) {
		Collection<T> annotations = index.get(t);
		if (annotations == null) {
			annotations = indexAnnotationStorageSupplier.get();
			index.put(t, annotations);
		}
		annotations.add(a);
	}

	/**
	 * <p>
	 * Generates search terms from <tt>a</tt> via the
	 * {@link #searchTermGenerator}. These terms are then used to lookup
	 * annotations in the index and returned.
	 * </p>
	 * <p>
	 * It is perfectly valid and actually a frequent usecase to search for
	 * annotations which are not themselves part of the index. When searching,
	 * for example, for (parts of) the covered text, one can search for an
	 * entity and retrieve tokens matching the entity's name.
	 * </p>
	 * 
	 * @param a
	 *            The annotation that provides search terms to search for.
	 * @return The found annotations.
	 */
	@SuppressWarnings("unchecked")
	public Stream<T> search(Annotation a) {
		Object o = searchTermGenerator.generateIndexTerms(a);
		if (o instanceof Stream) {
			Stream<K> searchTerms = (Stream<K>) o;
			return search(searchTerms);
		} else {
			return search((K) o);
		}
	}

	/**
	 * Searches for the provided search terms in the index.
	 * 
	 * @param searchTerms
	 *            The terms used to look up annotations.
	 * @return The found annotations.
	 */
	public Stream<T> search(Stream<K> searchTerms) {
		Stream<T> hits = null;
		for (Iterator<K> it = searchTerms.iterator(); it.hasNext();) {
			K t = it.next();
			Collection<T> hit = index.get(t);
			if (hit != null) {
				if (hits == null)
					hits = hit.stream();
				else
					Stream.concat(hits, hit.stream());
			}
		}

		return hits;
	}

	/**
	 * Searches for annotations in the index by the provided search term.
	 * 
	 * @param searchTerm
	 *            The term to search for.
	 * @return The found annotations.
	 */
	public Stream<T> search(K searchTerm) {
		Collection<T> collection = index.get(searchTerm);
		return collection != null ? collection.stream() : Stream.empty();
	}

	public T getFirst(K searchTerm) {
		Collection<T> collection = index.get(searchTerm);
		if (collection == null)
			return null;
		Iterator<T> it = collection.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	@SuppressWarnings("unchecked")
	public T getFirst(Annotation a) {
		Object o = searchTermGenerator.generateIndexTerms(a);
		if (o instanceof Stream) {
			T result = null;
			Stream<K> searchTerms = (Stream<K>) o;
			try {
				for (Iterator<K> it = searchTerms.iterator(); it.hasNext();) {
					K term = it.next();
					Collection<T> annotations = index.get(term);
					Iterator<T> annIt = annotations.iterator();
					if (annIt.hasNext()) {
						result = annIt.next();
						return result;
					}
				}
			} finally {
				searchTerms.close();
			}
			return result;
		} else {
			return getFirst((K) o);
		}
	}

	public T get(K searchTerm) {
		Collection<T> collection = index.get(searchTerm);
		if (collection == null)
			return null;
		Iterator<T> it = collection.iterator();
		try {
			if (it.hasNext())
				return it.next();
			return null;
		} finally {
			if (it.hasNext())
				throw new IllegalStateException("There are multiple values associated with key \"" + searchTerm
						+ "\". Use the search(K) method.");
		}
	}

	@SuppressWarnings("unchecked")
	public T get(Annotation a) {
		Object o = searchTermGenerator.generateIndexTerms(a);
		if (o instanceof Stream) {
			T result = null;
			Stream<K> searchTerms = (Stream<K>) o;
			try {
				for (Iterator<K> it = searchTerms.iterator(); it.hasNext();) {
					K term = it.next();
					Collection<T> annotations = index.get(term);
					if (annotations != null && result == null) {
						Iterator<T> annIt = annotations.iterator();
						if (annIt.hasNext())
							result = annIt.next();
						if (it.hasNext())
							throw new IllegalStateException("There are multiple values associated with key \"" + term
									+ "\". Use the search(Annotation) method.");
					} else if (annotations != null)
						throw new IllegalStateException("Multiple search terms produced search hits for annotation " + a
								+ ". Use the search(Annotation) method.");
				}
			} finally {
				searchTerms.close();
			}
			return result;
		} else {
			return get((K) o);
		}
	}

	public Map<K, Collection<T>> getIndex() {
		return index;
	}

	/**
	 * Allows to change the supplier for the internal storage of annotations
	 * which are the values of the index map. That might be helpful when one
	 * wants to control the storage strategy in order to be able to predict the
	 * shape of search results.
	 * 
	 * @param supplier
	 *            A supplier that will be used to create the internal storage
	 *            for indexed annotations (i.e. the values of the index map).
	 */
	public void setIndexAnnotationStorageSupplier(Supplier<Collection<T>> supplier) {
		if (!index.isEmpty())
			throw new IllegalStateException(
					"The index must be empty when the supplier for the internal storage of annotations is changed.");
		this.indexAnnotationStorageSupplier = supplier;
	}

	@Override
	public void add(T a) {
		index(a);
	}

}
