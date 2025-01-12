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
package de.julielab.jcore.reader.pmc.parser;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;

public class SectionParserTest {

	private static final Logger log = LoggerFactory.getLogger(SectionParserTest.class);

	@Test
	public void testParser() throws Exception {
		// this publication does not define title for all sections
		JCas cas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-document-meta-pubmed-types", "de.julielab.jcore.types.jcore-document-structure-pubmed-types");
		NxmlDocumentParser documentParser = new NxmlDocumentParser();
		documentParser.loadElementPropertyFile("/de/julielab/jcore/reader/pmc/resources/elementproperties.yml");
		File inputFile = new File("src/test/resources/documents-misc/PMC5289004.nxml.gz");
		documentParser.reset(inputFile, cas);

		String documentText = IOUtils.toString(new GZIPInputStream(new FileInputStream(inputFile)));

		SectionParser sectionParser = new SectionParser(documentParser);
		VTDNav vn = documentParser.getVn();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//sec");
		while (ap.evalXPath() != -1) {
			int start = vn.getTokenOffset(vn.getCurrentIndex());
			int end = (int) vn.getOffsetAfterHead();
			log.debug(documentText.substring(start, end));
			ElementParsingResult secResult = sectionParser.parse();
			assertNotNull(secResult);
		}
	}

	@Test
	public void testParser2() throws Exception {
		// this publication does not define pages but an electronic location
		JCas cas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-document-meta-pubmed-types", "de.julielab.jcore.types.jcore-document-structure-pubmed-types");
		NxmlDocumentParser documentParser = new NxmlDocumentParser();
		documentParser.loadElementPropertyFile("/de/julielab/jcore/reader/pmc/resources/elementproperties.yml");
		File inputFile = new File("src/test/resources/documents-misc/PMC2836310.nxml.gz");
		documentParser.reset(inputFile, cas);

		SectionParser sectionParser = new SectionParser(documentParser);
		VTDNav vn = documentParser.getVn();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//sec");
		while (ap.evalXPath() != -1) {
			ElementParsingResult secResult = sectionParser.parse();
			assertNotNull(secResult);
		}
	}
}
