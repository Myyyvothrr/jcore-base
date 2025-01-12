/**
 * FeatureGenerator.java
 *
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: tomanek
 *
 * Current version: 2.3
 * Since version:   2.2
 *
 * Creation date: Nov 1, 2006
 *
 * Generating features for given text. Text is given as an ArrayList of Sentence objects that is,
 * an ArrayList of ArrayLists of Unit objects.
 **/

package de.julielab.jcore.ae.jpos.pipes;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.*;
import cc.mallet.types.*;
import de.julielab.jcore.ae.jpos.tagger.FeatureConfiguration;
import de.julielab.jcore.ae.jpos.tagger.Sentence;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

public class FeatureGenerator {

	private final static String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega)";

	private final static String UNICODE_UPPER = "\\p{Lu}";;
	private final static String UNICODE_LOWER = "\\p{Ll}";

	public InstanceList createFeatureData(final ArrayList<Sentence> sentences,
			final Properties featureConfig) {

		final FeatureConfiguration fc = new FeatureConfiguration();
		final ArrayList<Pipe> pipeParam = new ArrayList<Pipe>();

		// base pipe
		pipeParam.add(new BasePipe(featureConfig));

		// default surface patterns
		pipeParam.add(new RegexMatches("INITLOWCAPS_ANYTHING_NONUMBER", Pattern
				.compile("[" + UNICODE_LOWER + "][" + UNICODE_UPPER
						+ "][^0-9]*")));
		pipeParam.add(new RegexMatches("INITLOWCAPS_ANYTHING_WITHNUMBER",
				Pattern.compile("[" + UNICODE_LOWER + "][" + UNICODE_UPPER
						+ "].*[0-9].*")));
		pipeParam.add(new RegexMatches("INITCAPS", Pattern.compile("["
				+ UNICODE_UPPER + "].*")));
		pipeParam.add(new RegexMatches("INITCAPSALPHA", Pattern.compile("["
				+ UNICODE_UPPER + "][" + UNICODE_LOWER + "].*")));
		pipeParam.add(new RegexMatches("ALLCAPS", Pattern.compile("["
				+ UNICODE_UPPER + "]+")));
		pipeParam.add(new RegexMatches("CAPSMIX", Pattern.compile("["
				+ UNICODE_UPPER + UNICODE_LOWER + "]+")));
		pipeParam
		.add(new RegexMatches("HASDIGIT", Pattern.compile(".*[0-9].*")));
		pipeParam
		.add(new RegexMatches("SINGLEDIGIT", Pattern.compile("[0-9]")));
		pipeParam.add(new RegexMatches("DOUBLEDIGIT", Pattern
				.compile("[0-9][0-9]")));
		pipeParam.add(new RegexMatches("NATURALNUMBER", Pattern
				.compile("[0-9]+")));
		pipeParam.add(new RegexMatches("REALNUMBER", Pattern
				.compile("[-0-9]+[.,]+[0-9.,]+")));
		pipeParam.add(new RegexMatches("HASDASH", Pattern.compile(".*-.*")));
		pipeParam.add(new RegexMatches("INITDASH", Pattern.compile("-.*")));
		pipeParam.add(new RegexMatches("ENDDASH", Pattern.compile(".*-")));
		pipeParam.add(new RegexMatches("ALPHANUMERIC", Pattern.compile(".*["
				+ UNICODE_UPPER + UNICODE_LOWER + "].*[0-9].*")));
		pipeParam
				.add(new RegexMatches("ALPHANUMERIC", Pattern
						.compile(".*[0-9].*[" + UNICODE_UPPER + UNICODE_LOWER
								+ "].*")));
		pipeParam.add(new RegexMatches("IS_PUNCTUATION_MARK", Pattern
				.compile("[,.;:?!]")));

		pipeParam.add(new RegexMatches("IS_MINUSDASHSLASH", Pattern
				.compile("[-_/]")));

		// bio surface patterns
		if (fc.featureActive(featureConfig, "feat_bioregexp_enabled")) {
			pipeParam.add(new RegexMatches("ROMAN", Pattern
					.compile("[IVXDLCM]+")));
			pipeParam.add(new RegexMatches("HASROMAN", Pattern
					.compile(".*\\b[IVXDLCM]+\\b.*")));
			pipeParam.add(new RegexMatches("GREEK", Pattern.compile(GREEK)));
			pipeParam.add(new RegexMatches("HASGREEK", Pattern.compile(".*\\b"
					+ GREEK + "\\b.*")));
		}

		// prefix and suffix
		final int[] prefixSizes = fc.getIntArray(featureConfig, "prefix_sizes");
		if (prefixSizes != null)
			for (final int prefixSize : prefixSizes)
				pipeParam.add(new TokenTextCharPrefix("PREFIX=", prefixSize));
		final int[] suffixSizes = fc.getIntArray(featureConfig, "suffix_sizes");
		if (suffixSizes != null)
			for (final int suffixSize : suffixSizes)
				pipeParam.add(new TokenTextCharSuffix("SUFFIX=", suffixSize));

		// lexicon membership
		for (final String key : fc.getLexiconKeys(featureConfig)) {
			final File lexFile = new File(featureConfig.getProperty(key));
			try {
				pipeParam.add(new LexiconMembership(key + "_membership",
						lexFile, true));
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// offset conjunction
		final int[][] offset = fc.offsetConjFromConfig(featureConfig
				.getProperty("offset_conjunctions"));
		if (offset != null)
			pipeParam.add(new OffsetConjunctions(offset));

		// token ngrams
		final int[] tokenNGrams = fc.getIntArray(featureConfig, "token_ngrams");
		if (tokenNGrams != null)
			pipeParam.add(new TokenNGramPipe(tokenNGrams));

		// character ngrams
		final int[] charNGrams = fc.getIntArray(featureConfig, "char_ngrams");
		if (charNGrams != null)
			pipeParam.add(new TokenTextCharNGrams("CHAR_NGRAM=", charNGrams));

		// un-comment this for printing out the generated features
		// pipeParam.add(new PrintTokenSequenceFeatures());

		pipeParam.add(new TokenSequence2FeatureVectorSequence(true, true));

		final Pipe[] pipeParamArray = new Pipe[pipeParam.size()];
		pipeParam.toArray(pipeParamArray);
		final Pipe myPipe = new SerialPipes(pipeParamArray);

		// now run data through pipes
		final InstanceList data = new InstanceList(myPipe);
		final SentencePipeIterator iterator = new SentencePipeIterator(
				sentences);
		data.addThruPipe(iterator);

		return data;
	}

	/**
	 * converts the sentence based instance list into a token based one This is
	 * needed for the ME-version of JET (JetMeClassifier)
	 *
	 * @param METrainerDummyPipe
	 * @param orgList
	 *            the sentence based instance list
	 * @return
	 */
	public static InstanceList convertFeatsforClassifier(
			final Pipe METrainerDummyPipe, final InstanceList orgList) {

		final InstanceList iList = new InstanceList(METrainerDummyPipe);

		for (int i = 0; i < orgList.size(); i++) {
			final Instance inst = orgList.get(i);

			final FeatureVectorSequence fvs = (FeatureVectorSequence) inst
					.getData();
			final LabelSequence ls = (LabelSequence) inst.getTarget();
			final LabelAlphabet ldict = (LabelAlphabet) ls.getAlphabet();
			final Object source = inst.getSource();
			final Object name = inst.getName();

			if (ls.size() != fvs.size()) {
				System.err
				.println("failed making token instances: size of labelsequence != size of featue vector sequence: "
						+ ls.size() + " - " + fvs.size());
				System.exit(-1);
			}
			for (int j = 0; j < fvs.size(); j++) {
				final Instance I = new Instance(fvs.getFeatureVector(j),
						ldict.lookupLabel(ls.get(j)), name, source);
				iList.add(I);
			}

		}
		return iList;
	}

	/**
	 * converts the sentence based instance list into a token based one This is
	 * needed for the ME-version of JET (JetMeClassifier)
	 *
	 * @param METrainerDummyPipe
	 * @param inst
	 *            just the features for one sentence to be transformed
	 * @return
	 */
	public static InstanceList convertFeatsforClassifier(
			final Pipe METrainerDummyPipe, final Instance inst) {

		final InstanceList iList = new InstanceList(METrainerDummyPipe);

		final FeatureVectorSequence fvs = (FeatureVectorSequence) inst
				.getData();
		final LabelSequence ls = (LabelSequence) inst.getTarget();
		final LabelAlphabet ldict = (LabelAlphabet) ls.getAlphabet();
		final Object source = inst.getSource();
		final Object name = inst.getName();

		if (ls.size() != fvs.size()) {
			System.err
			.println("failed making token instances: size of labelsequence != size of featue vector sequence: "
					+ ls.size() + " - " + fvs.size());
			System.exit(-1);
		}

		for (int j = 0; j < fvs.size(); j++) {
			final Instance I = new Instance(fvs.getFeatureVector(j),
					ldict.lookupLabel(ls.get(j)), name, source);
			iList.add(I);

		}

		return iList;
	}

}
