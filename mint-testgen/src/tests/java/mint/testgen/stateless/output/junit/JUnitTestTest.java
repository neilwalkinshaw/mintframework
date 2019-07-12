package mint.testgen.stateless.output.junit;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

public class JUnitTestTest {

    @Test
    public void testGeneratorTester(){
        try {
            JUnitTest sut = new JUnitTest("testOutput","test","testOutput","BioJavaSUT","measureAlignmentFromFile");
            //sut.addTest("\"src/test/resources/1N1Z.fasta.txt\"");
            sut.write();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

}