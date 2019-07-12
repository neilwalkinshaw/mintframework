package mint.testgen.stateless.output.junit;

import mint.testgen.stateless.output.TestRecorder;
import mint.testgen.stateless.runners.execution.TestRunner;
import mint.tracedata.TestIO;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class JUnitTestRecorder implements TestRecorder {

    String path, packageName, clazz, method;

    public JUnitTestRecorder(TestRunner.JUnitDetails details){
        this.path=details.getPath();
        this.packageName=details.getPackageName();
        this.clazz=details.getClazz();
        this.method=details.getMethod();
    }


    @Override
    public void record(List<TestIO> input, List<Integer> iterations) {
        try {

            Iterator<Integer> iterIt = iterations.iterator();
            int nextBreak = input.size();
            int counter = 0;
            int testNameCounter = 0;
            if (iterIt.hasNext())
                nextBreak = iterIt.next();
            JUnitTest jut = new JUnitTest(path, "Test" + testNameCounter, packageName, clazz, method);
            for (TestIO io : input) {
                jut.addTest(io);
                counter++;
                if (counter == nextBreak) {
                    jut.write();
                    testNameCounter++;
                    jut = new JUnitTest(path, "Test" + testNameCounter, packageName, clazz, method);
                    if (iterIt.hasNext())
                        nextBreak = iterIt.next();
                    else
                        nextBreak = input.size();

                }

            }
        }

        catch(ClassNotFoundException e){
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
