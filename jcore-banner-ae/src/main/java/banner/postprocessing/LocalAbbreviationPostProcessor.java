package banner.postprocessing;

import banner.postprocessing.ExtractAbbrev.AbbreviationPair;
import banner.types.EntityType;
import banner.types.Mention;
import banner.types.Sentence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class LocalAbbreviationPostProcessor implements PostProcessor {
    private final static Logger log = LoggerFactory.getLogger(LocalAbbreviationPostProcessor.class);
    private ExtractAbbrev extractAbbrev;

    public LocalAbbreviationPostProcessor() {
        extractAbbrev = new ExtractAbbrev();
    }

    private void processAbbreviation(Mention formFound, String formNotFound) {
        Sentence sentence = formFound.getSentence();
        EntityType type = formFound.getEntityType();
        int charIndex = sentence.getText().indexOf(formNotFound);
        int start = sentence.getTokenIndex(charIndex, true);
        int end = sentence.getTokenIndex(charIndex + formNotFound.length(), false);
        if (start == end)
            return;
        Mention newMention = null;
        try {
            newMention = new Mention(sentence, start, end, type, formFound.getMentionType(), formFound.getProbability());
        } catch (Exception e) {
            log.warn("Exception occurred while creating a new entity mention for an abbrevation pair." +
                            " The string to be tagged as another mention is \"{}\", its character offset in the text" +
                            " sentence was determined as {}, its start token index as {} and its end token index as {}." +
                            "No new mention will be added and processing will continue.", formNotFound,
                    charIndex,
                    start, end);
        }
        if (newMention != null) {
            boolean overlaps = false;
            for (Mention mention : sentence.getMentions())
                overlaps |= mention.overlaps(newMention);
            if (!overlaps)
                sentence.addMention(newMention);
        }
    }

    public void postProcess(Sentence sentence) {
        Set<AbbreviationPair> abbreviationPairs = extractAbbrev.extractAbbrPairs(sentence.getText());
        if (abbreviationPairs.size() > 0) {
            for (AbbreviationPair abbreviation : abbreviationPairs) {
                Mention shortMention = null;
                Mention longMention = null;
                for (Mention mention : sentence.getMentions()) {
                    if (abbreviation.getShortForm().equals(mention.getText()))
                        shortMention = mention;
                    if (abbreviation.getLongForm().equals(mention.getText()))
                        longMention = mention;
                }
                if (shortMention == null) {
                    if (longMention != null)
                        processAbbreviation(longMention, abbreviation.getShortForm());
                } else {
                    if (longMention == null)
                        processAbbreviation(shortMention, abbreviation.getLongForm());
                }
            }
        }
    }

}
