package mint.evaluation.coberturaAnalysis;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Takes as input two sets of cobertura xml files (corresponding to results from
 * two types of test generation). Compares coverage for both.
 *
 * Produces a CSV file for external visualisation.
 *
 * Created by neilwalkinshaw on 13/07/2017.
 */
public class SeqLengthComparison {

    protected final int iterations;
    private final static Logger LOGGER = Logger.getLogger(SeqLengthComparison.class.getName());

    private List<Integer> seqLength_A, seqLength_B;

    public SeqLengthComparison(String root_A, String root_B, int iterations){
        seqLength_A = new ArrayList<Integer>();
        seqLength_B = new ArrayList<Integer>();
        this.iterations = iterations;
        compare(root_A, root_B);
    }

    public List<Integer> getCovered_A(){
        return seqLength_A;
    }

    public List<Integer> getCovered_B(){
        return seqLength_B;
    }


    private void compare(String root_A, String root_B) {
        for(int i = 0; i< iterations; i++) {
            String rootDir_A = root_A + File.separator + Integer.toString(i);
            Integer coverageA = getSeqLength(new File(rootDir_A));
            seqLength_A.add(coverageA);
            String rootDir_B = root_B + File.separator + Integer.toString(i);
            Integer coverageB = getSeqLength(new File(rootDir_B));
            seqLength_B.add(coverageB);
        }
    }

    private int getSeqLength(File root_A) {
        String sequence = null;
        try {
            File seqFile = new File(root_A.getCanonicalPath() + File.separator + "sequence");
            BufferedReader reader = new BufferedReader(new FileReader(seqFile));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            try {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                sequence = stringBuilder.toString();
            } finally {
                reader.close();
            }
        }
        catch(Exception e){
            LOGGER.error(e.getMessage());
        }
        StringTokenizer tokenizer = new StringTokenizer(sequence,",");

        return tokenizer.countTokens();
    }


    public static void main(String[] args){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        String from = args[0];
        String to = args[1];
        int iterations = Integer.parseInt(args[2]);
        SeqLengthComparison cc = new SeqLengthComparison(from,to,iterations);
        List<Integer> covered_A= cc.getCovered_A();
        List<Integer> covered_B= cc.getCovered_B();

        for(int i = 0; i<iterations; i++){
            System.out.println(covered_A.get(i)+", "+covered_B.get(i));
        }
    }

}
