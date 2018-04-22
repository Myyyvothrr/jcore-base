package de.julielab.jcore.reader.db;

import de.julielab.jcore.types.casmultiplier.RowBatch;
import de.julielab.xmlData.dataBase.DBCIterator;
import de.julielab.xmlData.dataBase.DataBaseConnector;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * A multiplier retrieving feature structures of type of {@link RowBatch} in its {@link #process(JCas)} method.
 * Each <code>RowBatch</code> lists IDs of documents to read and the table to read them from.
 * The part of actual reading the documents into CAS instances is subject to implementation for extending classes.
 * For this purpose, the iterator holding the actual document data, {@link #documentDataIterator}, must be used
 * to retrieve document data and use it to populate CAS instances in the {@link JCasMultiplier_ImplBase#next()}
 * method.
 */
@ResourceMetaData( name = "JCoRe Abstract Database Multiplier", description = "A multiplier that receives document IDs to read from a database table from the " +
        "DBMultiplierReader. The reader also delivers the path to the corpus storage system (CoStoSys) configuration and additional tables " +
        "for joining with the main data table. This multiplier class is abstract and cannot be used directly." +
        "Extending classes must implement the next() method to actually read documents from the database and " +
        "populate CASes with them. This component is a part of the JeDIS system.")
public abstract class DBMultiplier extends JCasMultiplier_ImplBase {

    protected DataBaseConnector dbc;
    protected DBCIterator<byte[][]> documentDataIterator;
    // This is set anew with every call to process()
    private boolean initialized;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        initialized = false;
    }

    private DataBaseConnector getDataBaseConnector(String costosysConfig) throws AnalysisEngineProcessException {
        DataBaseConnector dbc;
        try {
            dbc = new DataBaseConnector(costosysConfig);
        } catch (FileNotFoundException e) {
            throw new AnalysisEngineProcessException(e);
        }
        return dbc;
    }


    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        RowBatch rowbatch = JCasUtil.selectSingle(aJCas, RowBatch.class);
        if (!initialized) {
            dbc = getDataBaseConnector(rowbatch.getCostosysConfiguration());
            initialized = true;
        }
        List<Object[]> documentIdsForQuery = new ArrayList<>();
        FSArray identifiers = rowbatch.getIdentifiers();
        for (int i = 0; i < identifiers.size(); i++) {
            StringArray primaryKey = (StringArray) identifiers.get(i);
            documentIdsForQuery.add(primaryKey.toArray());
        }
        documentDataIterator = dbc.retrieveColumnsByTableSchema(documentIdsForQuery,
                rowbatch.getTables().toStringArray(),
                rowbatch.getTableSchemas().toStringArray());
    }

    @Override
    public boolean hasNext() {
        return documentDataIterator != null && documentDataIterator.hasNext();
    }
}