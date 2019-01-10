package de.julielab.jcore.ae.mutationfinder;

import edu.uchsc.ccp.nlp.ei.mutation.Mutation;
import edu.uchsc.ccp.nlp.ei.mutation.MutationException;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import edu.uchsc.ccp.nlp.ei.mutation.PointMutation;
import org.apache.uima.Constants;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@ResourceMetaData(name="JCoRe Mutation Annotator", vendor = "JULIE Lab, Germany",description = "An analysis engine to recognize " +
        "mentions of gene point mutations in document text. This is a wrapper around the original MutationFinder " +
        "(http://mutationfinder.sourceforge.net/), published in the following paper: " +
        "MutationFinder: A high-performance system for extracting point mutation mentions from text\n" +
        "J. Gregory Caporaso, William A. Baumgartner Jr., David A. Randolph, K. Bretonnel Cohen, and Lawrence Hunter; Bioinformatics, 2007 23(14):1862-1865; doi:10.1093/bioinformatics/btm235;")
@TypeCapability(outputs = {"de.julielab.jcore.types.PointMutation"})
public class MutationAnnotator extends JCasAnnotator_ImplBase {

    private MutationFinder mf;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        try {
            mf = new MutationFinder(MutationAnnotator.class.getResourceAsStream("/regex.txt"));
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            final Map<Mutation, Set<int[]>> mutations = mf.extractMutations(jCas.getDocumentText());
            for (Mutation mutation : mutations.keySet()) {
                final Set<int[]> locations = mutations.get(mutation);
                locations.forEach(location -> {
                    final de.julielab.jcore.types.PointMutation pmAnnotation = new de.julielab.jcore.types.PointMutation(jCas, location[0], location[1]);
                    pmAnnotation.setSpecificType(mutation.toString());
                    pmAnnotation.setComponentId(MutationAnnotator.class.getCanonicalName());
                    pmAnnotation.addToIndexes();
                });
            }
        } catch (MutationException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}