package mint.testgen.stateless.text;

import mint.Configuration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Created by neilwalkinshaw on 28/06/2018.
 */
public class TextIORunnerTest {



    @Test
    public void textIORunner(){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        Configuration.getInstance().JAVA_SUT="output,BioJavaSUT,measureAlignmentFromFile";
        Configuration.getInstance().ITERATIONS=30;
        Configuration.getInstance().QBC_ITERATIONS=5;
        //TextIORunner sut = new TextIORunner("src/tests/resources/biojavaSpec",
        //        "src/tests/resources/testFASTAFilesDownloaded","src/tests/resources/testFASTAFilesGT");

        TextIORunner sut = new TextIORunner("src/tests/resources/biojavaSpec",
                        "/tmp/testFASTAFilesDownloaded","/tmp/fasta_gen_04_data_180629_10kdatums");
        sut.recordPerformance();
        sut.run();
    }

    /*@Test
    public void textIORandomRunner(){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        Configuration.getInstance().JAVA_SUT="output,BioJavaSUT,measureAlignmentFromFile";
        Configuration.getInstance().ITERATIONS=30;
        Configuration.getInstance().QBC_ITERATIONS=5;

        TextIORunner sut = new TextIORunner("src/tests/resources/biojavaSpec",
                "/tmp/testFASTAFilesDownloaded","/tmp/fasta_gen_04_data_180629_10kdatums");
        sut.recordPerformance();
        sut.setRandomTests(true);
        sut.run();
    }*/

}