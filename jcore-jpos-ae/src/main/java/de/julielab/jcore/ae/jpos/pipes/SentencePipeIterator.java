/**
 * SentencePipeIterator.java
 *
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: tomanek
 *
 * Current version: 2.4
 * Since version:   2.2
 *
 * Creation date: Dec 7, 2006
 *
 * this is a PipeInputIterator that iterates over Sentence objects
 * and is used to fill an InstanceList.
 *
 * As input, this iterator expects an ArrayList of Sentence object. For each
 * of these objects, the iterator creates an Instance (the data field is a Sentence object).
 **/

package de.julielab.jcore.ae.jpos.pipes;

import cc.mallet.types.Instance;
import de.julielab.jcore.ae.jpos.tagger.Sentence;

import java.util.ArrayList;
import java.util.Iterator;

public class SentencePipeIterator implements Iterator<Instance> {

	private final Iterator<Sentence> sentIterator;

	public SentencePipeIterator(final ArrayList<Sentence> sentences) {
		sentIterator = sentences.iterator();
	}

	@Override
	public boolean hasNext() {
		return sentIterator.hasNext();
	}

	@Override
	public Instance next() {
		final Sentence sent = sentIterator.next();
		final Instance inst = new Instance(sent, "", "", "");
		return inst;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Method not supported");
	}
}