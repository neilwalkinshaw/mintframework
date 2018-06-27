package mint.inference.text.doc2vec;

import org.apache.commons.lang.StringUtils;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 20/06/2018.
 */
public class Doc2Vec {

    protected ParagraphVectors vec;

    public Doc2Vec(List<String> docs, List<String> labels, int layerSize){

        LabelAwareSentenceIterator iter = new GenericLabelAwareIterator(docs,labels,new SentencePreProcessor() {
                @Override
                public String preProcess(String sentence) {
                    sentence = sentence.replaceAll("[^\\w]", " ");
                    sentence = sentence.replaceAll("\\P{L}", " ");
                    sentence = sentence.replaceAll("( )+", " ");
                    String[] splitUp = StringUtils.splitByCharacterTypeCamelCase(sentence);
                    sentence = "";
                    for(String s : splitUp){
                        sentence += s+" ";
                    }
                    //System.out.println(sentence.toLowerCase());
                    return sentence.toLowerCase();
                }
            });
        TokenizerFactory t = new DefaultTokenizerFactory();
        vec = new ParagraphVectors.Builder()
                .minWordFrequency(4).layerSize(layerSize)
                .stopWords(new ArrayList<String>())
                .windowSize(5).iterate(iter).tokenizerFactory(t).build();

        vec.fit();

    }


    public ParagraphVectors getModel(){
        return vec;
    }
}
