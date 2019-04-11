
package de.julielab.jcore.consumer.ew;

import de.julielab.jcore.types.EmbeddingVector;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.utility.JCoReTools;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for jcore-flair-embedding-writer.
 *
 */
public class EmbeddingWriterTest {
    @Test
    public void testGetAverageEmbeddingVector() throws Exception{
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-morpho-syntax-types");
        Token t1 = new Token(jCas, 0, 1);
        final EmbeddingVector e1 = new EmbeddingVector(jCas, 0, 1);
        final DoubleArray v1 = new DoubleArray(jCas, 2);
        v1.set(0, 3);
        v1.set(1, 12);
        e1.setVector(v1);
        t1.setEmbeddingVectors(JCoReTools.addToFSArray(null, e1));

        Token t2 = new Token(jCas, 1, 2);
        final EmbeddingVector e2 = new EmbeddingVector(jCas, 1, 2);
        final DoubleArray v2 = new DoubleArray(jCas, 2);
        v2.set(0, 7);
        v2.set(1, 5);
        e2.setVector(v2);
        t2.setEmbeddingVectors(JCoReTools.addToFSArray(null, e2));

        final Stream<Token> stream = Stream.<Token>builder().add(t1).add(t2).build();

        final Method m = EmbeddingWriter.class.getDeclaredMethod("getAverageEmbeddingVector", Stream.class);
        m.setAccessible(true);
        final double[] avgVector = (double[]) m.invoke(new EmbeddingWriter(), stream);

        assertThat(avgVector).containsExactly(5, 8.5);
    }

    @Test
    public void testWriterAllTokens() throws Exception {
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-morpho-syntax-types");
        jCas.setDocumentText("t1 t2 t3");
        Token t1 = new Token(jCas, 0, 2);
        final EmbeddingVector e1 = new EmbeddingVector(jCas, 0, 2);
        final DoubleArray v1 = new DoubleArray(jCas, 2);
        v1.set(0, 3);
        v1.set(1, 12);
        e1.setVector(v1);
        t1.setEmbeddingVectors(JCoReTools.addToFSArray(null, e1));
        t1.addToIndexes();

        Token t2 = new Token(jCas, 3, 5);
        final EmbeddingVector e2 = new EmbeddingVector(jCas, 3, 5);
        final DoubleArray v2 = new DoubleArray(jCas, 2);
        v2.set(0, 7);
        v2.set(1, 5);
        e2.setVector(v2);
        t2.setEmbeddingVectors(JCoReTools.addToFSArray(null, e2));
        t2.addToIndexes();

        Token t3 = new Token(jCas, 6, 8);
        final EmbeddingVector e3 = new EmbeddingVector(jCas, 6, 8);
        final DoubleArray v3 = new DoubleArray(jCas, 2);
        v3.set(0, 45);
        v3.set(1, 13);
        e3.setVector(v3);
        t3.setEmbeddingVectors(JCoReTools.addToFSArray(null, e3));
        t3.addToIndexes();

        final AnalysisEngine engine = AnalysisEngineFactory.createEngine("de.julielab.jcore.consumer.ew.desc.jcore-embedding-writer", EmbeddingWriter.PARAM_OUTDIR, "src/test/resources/output");

        engine.process(jCas);
        engine.collectionProcessComplete();
    }
}
