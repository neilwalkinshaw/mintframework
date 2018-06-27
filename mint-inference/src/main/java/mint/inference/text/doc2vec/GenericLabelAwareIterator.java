package mint.inference.text.doc2vec;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by neilwalkinshaw on 20/06/2018.
 */
public class GenericLabelAwareIterator  implements LabelAwareSentenceIterator {

    private List<String> phrases;
    private List<String> labels;
    private AtomicInteger currRecord;
    private SentencePreProcessor preProcessor;

    public GenericLabelAwareIterator(List<String> phrases, List<String> labels, SentencePreProcessor preProcessor){
        this.phrases = phrases;
        this.labels=labels;
        this.preProcessor = preProcessor;
        currRecord = new AtomicInteger(0);
    }

    @Override
    public String currentLabel() {
        return labels.get(currRecord.get() > 0 ? currRecord.get() - 1 : 0);
    }

    @Override
    public List<String> currentLabels() {
        return labels;
    }

    @Override
    public String nextSentence() {
        String ret = phrases.get(currRecord.get());
        ret = preProcessor.preProcess(ret);
        currRecord.incrementAndGet();
        return ret;
    }

    @Override
    public boolean hasNext() {
        return currRecord.get() < phrases.size();
    }

    @Override
    public void reset() {
        currRecord.set(0);
    }

    @Override
    public void finish() {

    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor sentencePreProcessor) {
        this.preProcessor = sentencePreProcessor;
    }
}
