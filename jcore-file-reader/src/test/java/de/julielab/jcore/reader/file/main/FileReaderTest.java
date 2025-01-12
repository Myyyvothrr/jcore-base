/**
 * FileReaderTest.java
 *
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: muehlhausen
 *
 * Current version: 1.0 Since version: 1.0
 *
 * Creation date: 27.08.2007
 *
 * Tests for class <code>FileReader</code>, a UIMA <code>CollctionReader</code>.
 */

package de.julielab.jcore.reader.file.main;

import de.julielab.jcore.types.Date;
import de.julielab.jcore.types.Sentence;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.pubmed.Header;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.cas.*;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileReaderTest {

	/**
	 * Path to the FileReader descriptor
	 */
	private static final String DESC_FILE_READER = "src/main/resources/de/julielab/jcore/reader/file/desc/jcore-file-reader.xml";

	private static CAS cas;

	/**
	 * Test data
	 */
	private final static String ARTIFACT_1 = "Our understanding of the role of interleukin (IL)-12 in controlling"
			+ " tuberculosis has expanded because of increased interest in other members of the IL-12 family of cytokines."
			+ " Recent data show that IL-12, IL-23 and IL-27 have specific roles in the initiation, expansion and control of"
			+ " the cellular response to tuberculosis. Specifically, IL-12, and to a lesser degree IL-23, generates protective"
			+ " cellular responses and promotes survival, whereas IL-27 moderates the inflammatory response and is required for"
			+ " long-term survival. Paradoxically, IL-27 also limits bacterial control, suggesting that a balance between"
			+ " bacterial killing and tissue damage is required for survival. Understanding the balance between IL-12, IL-23 and"
			+ " IL-27 is crucial to the development of immune intervention in tuberculosis. ";

	private static final String FILE_ARTIFACT_1 = "data/files/8563171.txt";

	private final static String ARTIFACT_2 = "Our understanding of the role of interleukin (IL)-12 in controlling"
			+ " tuberculosis has expanded because of increased interest in other members of the IL-12 family of cytokines.\n"
			+ "Recent data show that IL-12, IL-23 and IL-27 have specific roles in the initiation, expansion and control of"
			+ " the cellular response to tuberculosis.\n"
			+ "Specifically, IL-12, and to a lesser degree IL-23, generates protective"
			+ " cellular responses and promotes survival, whereas IL-27 moderates the inflammatory response and is required for"
			+ " long-term survival.\n"
			+ "Paradoxically, IL-27 also limits bacterial control, suggesting that a balance between"
			+ " bacterial killing and tissue damage is required for survival.\n"
			+ "Understanding the balance between IL-12, IL-23 and"
			+ " IL-27 is crucial to the development of immune intervention in tuberculosis.";

	private static final String FILE_ARTIFACT_2 = "data/sentPerLine/8563171.txt";

	private static final Integer S_GOLD_COUNT = new Integer(5);// number of
																// sentences in
																// ARTIFACT_2

	private final static String ARTIFACT_3 = "Our understanding of the role of interleukin (IL)-12 in controlling"
			+ " tuberculosis has expanded because of increased interest in other members of the IL-12 family of cytokines .\n"
			+ "Recent data show that IL-12 , IL-23 and IL-27 have specific roles in the initiation , expansion and control of"
			+ " the cellular response to tuberculosis .\n"
			+ "Specifically , IL-12 , and to a lesser degree IL-23 , generates protective"
			+ " cellular responses and promotes survival , whereas IL-27 moderates the inflammatory response and is required for"
			+ " long-term survival .\n"
			+ "Paradoxically , IL-27 also limits bacterial control , suggesting that a balance between"
			+ " bacterial killing and tissue damage is required for survival .\n"
			+ "Understanding the balance between IL-12 , IL-23 and"
			+ " IL-27 is crucial to the development of immune intervention in tuberculosis .";

	private static final String FILE_ARTIFACT_3 = "data/sentPerLineAndToken/8563171.txt";

	 private final static String ARTIFACT_4 = "Our understanding of the role of interleukin (IL)-12 in controlling"
			+ " tuberculosis has expanded because of increased interest in other members of the IL-12 family of cytokines ."
			+ " Recent data show that IL-12 , IL-23 and IL-27 have specific roles in the initiation , expansion and control of"
			+ " the cellular response to tuberculosis . Specifically , IL-12 , and to a lesser degree IL-23 , generates protective"
			+ " cellular responses and promotes survival , whereas IL-27 moderates the inflammatory response and is required for"
			+ " long-term survival . Paradoxically , IL-27 also limits bacterial control , suggesting that a balance between"
			+ " bacterial killing and tissue damage is required for survival . Understanding the balance between IL-12 , IL-23 and"
			+ " IL-27 is crucial to the development of immune intervention in tuberculosis .";
	
	 private static final String FILE_ARTIFACT_4 = "data/onlyToken/8563171.txt";


	@BeforeClass

	public static void setUp() throws Exception {
		writeArtifact(ARTIFACT_1, FILE_ARTIFACT_1);
		writeArtifact(ARTIFACT_2, FILE_ARTIFACT_2);
		writeArtifact(ARTIFACT_3, FILE_ARTIFACT_3);
		writeArtifact(ARTIFACT_4, FILE_ARTIFACT_4);
	}

	@Test
	public void testDocumentTextPresent() throws CASException, Exception {
		CollectionReader fileReader = getCollectionReader(DESC_FILE_READER);
		fileReader.setConfigParameterValue("InputDirectory",
				FILE_ARTIFACT_1.substring(0, FILE_ARTIFACT_1.lastIndexOf("/")));
		fileReader.setConfigParameterValue("UseFilenameAsDocId", true);
		fileReader.setConfigParameterValue("PublicationDatesFile", "src/test/resources/data/BC2_publicationDates");
		fileReader.setConfigParameterValue(FileReader.ALLOWED_FILE_EXTENSIONS, new String[] { "txt" });
		fileReader.reconfigure();
		cas = CasCreationUtils.createCas((AnalysisEngineMetaData) fileReader.getMetaData());
		assertTrue(fileReader.hasNext());
		fileReader.getNext(cas);
		assertTrue(cas.getDocumentText().equals(ARTIFACT_1));

		Type headerType = cas.getTypeSystem().getType(Header.class.getCanonicalName());
		FSIterator<FeatureStructure> headerIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(headerType);
		assertTrue(headerIt.hasNext());
		Header header = (Header) headerIt.next();
		assertEquals("8563171", header.getDocId());

		Type dateType = cas.getTypeSystem().getType(Date.class.getCanonicalName());
		FSIterator<FeatureStructure> dateIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(dateType);
		assertTrue(dateIt.hasNext());
		Date date = (Date) dateIt.next();
		assertEquals(1995, date.getYear());
		assertEquals(10, date.getMonth());
		System.out.println(
				"pubmed-id: " + header.getDocId() + ", publication date: " + date.getYear() + "/" + date.getMonth());

	}

	@Test
	public void testSentencePerLineMode() throws CASException, Exception {
		CollectionReader fileReader = getCollectionReader(DESC_FILE_READER);
		fileReader.setConfigParameterValue("InputDirectory",
				FILE_ARTIFACT_2.substring(0, FILE_ARTIFACT_2.lastIndexOf("/")));
		fileReader.setConfigParameterValue("UseFilenameAsDocId", false);

		fileReader.setConfigParameterValue(FileReader.ALLOWED_FILE_EXTENSIONS, new String[] { "txt" });
		fileReader.setConfigParameterValue("SentencePerLine", true);
		fileReader.reconfigure();
		cas = CasCreationUtils.createCas((AnalysisEngineMetaData) fileReader.getMetaData());
		assertTrue(fileReader.hasNext());
		fileReader.getNext(cas);

		assertTrue(cas.getDocumentText().equals(ARTIFACT_2));

		Type sentType = cas.getTypeSystem().getType(Sentence.class.getCanonicalName());
		FSIterator<FeatureStructure> sentIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(sentType);
		Integer scount = 0;
		while (sentIt.hasNext()) {
			scount += 1;
			System.out.println("sent " + scount + ": " + ((Sentence) sentIt.next()).getCoveredText());
		}
		System.out.println("Sentences counted: " + scount.toString() + " -- Gold: " + S_GOLD_COUNT);
		assertEquals(S_GOLD_COUNT, scount);
	}

	@Test
	public void testSentencePerLineAndTokenizedMode() throws Exception {
		CollectionReader fileReader = getCollectionReader(DESC_FILE_READER);
		fileReader.setConfigParameterValue("InputDirectory",
				FILE_ARTIFACT_3.substring(0, FILE_ARTIFACT_3.lastIndexOf("/")));
		fileReader.setConfigParameterValue("UseFilenameAsDocId", false);
		fileReader.setConfigParameterValue(FileReader.ALLOWED_FILE_EXTENSIONS, new String[] { "txt" });
		fileReader.setConfigParameterValue("SentencePerLine", true);
		fileReader.setConfigParameterValue("TokenByToken", true);
		fileReader.reconfigure();
		cas = CasCreationUtils.createCas((AnalysisEngineMetaData) fileReader.getMetaData());
		assertTrue(fileReader.hasNext());
		fileReader.getNext(cas);
		assertTrue(cas.getDocumentText().equals(ARTIFACT_3));

		Type sentType = cas.getTypeSystem().getType(Sentence.class.getCanonicalName());
		FSIterator<FeatureStructure> sentIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(sentType);

		Integer scount = 0;
		while (sentIt.hasNext()) {
			scount += 1;
			System.out.println("sent " + scount + ": " + ((Sentence) sentIt.next()).getCoveredText());
		}

		System.out.println("Sentences counted: " + scount.toString() + " -- Gold: " + S_GOLD_COUNT);
		assertEquals(S_GOLD_COUNT, scount);

		Type tokType = cas.getTypeSystem().getType(Token.class.getCanonicalName());
		FSIterator<FeatureStructure> tokIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(tokType);

		Integer tcount = 0;
		while (tokIt.hasNext()) {
			tcount += 1;
			String x = ((Token) tokIt.next()).getCoveredText();
			System.out.println("tok " + tcount + ": " + x + "_" + x.length());
		}
		System.out.println("Tokens counted: " + tcount.toString() + " -- Gold " + 128);
	}
	
	@Test
	public void testOnlyTokenizedMode() throws Exception {
		CollectionReader fileReader = getCollectionReader(DESC_FILE_READER);
		fileReader.setConfigParameterValue("InputDirectory",
				FILE_ARTIFACT_4.substring(0, FILE_ARTIFACT_4.lastIndexOf("/")));
		fileReader.setConfigParameterValue("UseFilenameAsDocId", false);
		fileReader.setConfigParameterValue(FileReader.ALLOWED_FILE_EXTENSIONS, new String[] { "txt" });
		fileReader.setConfigParameterValue("TokenByToken", true);
		fileReader.reconfigure();
		cas = CasCreationUtils.createCas((AnalysisEngineMetaData) fileReader.getMetaData());
		assertTrue(fileReader.hasNext());
		fileReader.getNext(cas);
		assertTrue(cas.getDocumentText().equals(ARTIFACT_4));

		Type tokType = cas.getTypeSystem().getType(Token.class.getCanonicalName());
		FSIterator<FeatureStructure> tokIt = cas.getJCas().getFSIndexRepository().getAllIndexedFS(tokType);

		Integer tcount = 0;
		while (tokIt.hasNext()) {
			tcount += 1;
			String x = ((Token) tokIt.next()).getCoveredText();
			System.out.println("tok " + tcount + ": " + x + "_" + x.length());
		}
		System.out.println("Tokens counted: " + tcount.toString() + " -- Gold " + 128);
	}

	/**
	 * Produces a CollectionReader form the given descriptor file name
	 *
	 * @param descriptor
	 *            The path to the descriptor
	 * @return A collection reader
	 */
	public static CollectionReader getCollectionReader(String descriptor) {

		CollectionReader reader = null;
		XMLInputSource inputSource = null;
		ResourceSpecifier readerResourceSpecifier = null;
		try {
			inputSource = new XMLInputSource(descriptor);
			readerResourceSpecifier = UIMAFramework.getXMLParser().parseResourceSpecifier(inputSource);
			reader = UIMAFramework.produceCollectionReader(readerResourceSpecifier);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidXMLException e) {
			e.printStackTrace();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		return reader;
	}

	/**
	 * Write the artifact to a file.
	 *
	 * @param artifact
	 *            Text to be written to file
	 */
	private static void writeArtifact(String artifact, String file_name) {

		File artifactFile = new File(file_name);
		try (FileOutputStream outputStream = new FileOutputStream(artifactFile)) {
			outputStream.write(artifact.getBytes());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
