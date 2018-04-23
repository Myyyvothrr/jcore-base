/**
 * DBMedlineReader.java
 * <p>
 * Copyright (c) 2008, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are protected. Please contact JULIE Lab for further information.
 * <p>
 * Author: landefeld
 * <p>
 * Current version: 1.0
 * Since version:   1.9
 * <p>
 * Creation date: 15.09.2008
 * <p>
 * An UIMA CollcetionReader that implements DBReader (a class that gets Documents by DB-based informations) using the MedlineMapper
 **/

package de.julielab.jcore.reader.xml;

import de.julielab.jcore.reader.db.DBReader;
import de.julielab.jcore.reader.xmlmapper.mapper.XMLMapper;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.IOException;

/**
 * An UIMA CollectionReader that implements DBReader (a class that gets
 * Documents by DB-based informations) using the XMLMapper
 *
 * @author faessler
 */
@ResourceMetaData(name = "XML Database Reader", description = "A collection reader that employs the jcore-xml-mapper " +
        "to parse XML documents from a PostgreSQL database into CAS instances. For this purpose, the reader requires " +
        "a mapping file for the jcore-xml-mapper that defines how to map the document XML structure onto CAS types. " +
        "Which exact data is read from the database table is determines by the table schema that is configured as " +
        "active in the corpus storage system (CoStoSys) configuration file. If this table schema defines multiple " +
        "columns to be retrieved, the 'RowMapping' parameter must be set to specify how the extra columns are mapped " +
        "into the CAS. The reader also supports the joining with more tables to load data distributed across" +
        "multiple data tables, which then also must be mapped via the 'RowMapping'. This mechanism and " +
        "this component are part of the Jena Document Information System, JeDIS."
        , vendor = "JULIE Lab Jena, Germany", copyright = "JULIE Lab Jena, Germany")
public class XMLDBReader extends DBReader {

    public static final String PARAM_ROW_MAPPING = Initializer.PARAM_ROW_MAPPING;
    public static final String PARAM_MAPPING_FILE = Initializer.PARAM_MAPPING_FILE;
    /**
     * Mapper which maps medline XML to a CAS with the specified UIMA type system
     * via an XML configuration file.
     */
    protected XMLMapper xmlMapper;
    protected int totalDocumentCount;
    protected int processedDocuments;
    @ConfigurationParameter(name = PARAM_ROW_MAPPING)
    protected String[] rowMappingArray;
    @ConfigurationParameter(name = PARAM_MAPPING_FILE, mandatory = true)
    protected String mappingFileStr;
    private Row2CasMapper row2CasMapper;
    private CasPopulator casPopulator;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize();
        mappingFileStr = (String) getConfigParameterValue(PARAM_MAPPING_FILE);
        rowMappingArray = (String[]) getConfigParameterValue(PARAM_ROW_MAPPING);
        Initializer initializer = new Initializer(mappingFileStr, rowMappingArray, () -> getAllRetrievedColumns());
        row2CasMapper = initializer.getRow2CasMapper();
        xmlMapper = initializer.getXmlMapper();
        casPopulator = new CasPopulator(dbc, xmlMapper, row2CasMapper, rowMappingArray);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas
     * .CAS)
     */
    public void getNext(JCas jcas) throws IOException, CollectionException {
        byte[][] documentData = getNextArtifactData();
        populateCas(jcas, documentData);
    }

    private void populateCas(JCas jcas, byte[][] documentData) throws CollectionException {
        try {
            casPopulator.populateCas(jcas, documentData,
                    (docData, jCas) -> setDBProcessingMetaData(dbc, readDataTable, tableName, docData, jCas));
        } catch (CasPopulationException e) {
            throw new CollectionException(e);
        }
    }


    @Override
    public Progress[] getProgress() {
        return new Progress[]{new ProgressImpl(processedDocuments, totalDocumentCount, Progress.ENTITIES, true)};
    }

    @Override
    protected String getReaderComponentName() {
        return getClass().getSimpleName();
    }


}
