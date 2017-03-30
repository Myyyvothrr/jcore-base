package de.julielab.jcore.reader.pmc;

public class ElementProperties {
	/**
	 * Block elements are enclosed by line breaks in the CAS document text.
	 */
	public static final String BLOCK_ELEMENT = "block-element";
	/**
	 * Indicates that the respective element should be omitted. That means that neither for the element nor for any of its descendants parsing will happen.
	 */
	public static final String OMIT_ELEMENT = "omit-element";
	/**
	 * The UIMA annotation type that should be used to annotate the described element.
	 */
	public static final String TYPE = "type";
	
	public static final String TYPE_NONE = "none";
}
