package mint.testgen.stateless.weka;

import mint.Configuration;
import mint.testgen.stateless.text.TextIORunner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class UncertaintySamplingTestGeneratorTest {

    @Test
    public void uncertaintyRunner(){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        Configuration.getInstance().JAVA_SUT="output,org.apache.commons.math3.special.BesselJ,value";
        Configuration.getInstance().ITERATIONS=10;
        Configuration.getInstance().QBC_ITERATIONS=5;
        //TextIORunner sut = new TextIORunner("src/tests/resources/biojavaSpec",
        //        "src/tests/resources/testFASTAFilesDownloaded","src/tests/resources/testFASTAFilesGT");

        //TextIORunner sut = new TextIORunner("src/tests/resources/besseljSpec",
        //        "/tmp/testFASTAFilesDownloaded","/tmp/fasta_gen_04_data_180629_10kdatums");
        //sut.recordPerformance();
        WekaClassifierTestRunner wcr = new WekaClassifierTestRunner("src/tests/resources/besseljSpec",null,
        Configuration.Data.Bagging);
        wcr.run();
    }

}