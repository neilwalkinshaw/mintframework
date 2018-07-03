package mint.testgen.stateless.text;

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
        TextIORunner sut = new TextIORunner("src/tests/resources/biojavaSpec",
                "src/tests/resources/testFASTAFilesDownloaded","src/tests/resources/testFASTAFilesGT");
        sut.run();
    }

}