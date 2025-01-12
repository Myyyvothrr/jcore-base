/**
 * Copyright (c) 2015, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the  Eclipse Public License (EPL) v3.0
 */

package de.julielab.jcore.ae.mstparser.main;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;

import java.io.FileInputStream;

public class TestThread extends Thread {

    @Override
    public void run() {
        super.run();
        try {
            XMLInputSource descriptor = new XMLInputSource(MSTParserTest.DESCRIPTOR_MST_PARSER);
            ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(descriptor);
            AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);
            CAS cas = ae.newCAS();
            FileInputStream fis = new FileInputStream(MSTParserTest.TEST_XMI);
            XmiCasDeserializer.deserialize(fis, cas);
            JCas jcas = cas.getJCas();
            ae.process(jcas);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}
