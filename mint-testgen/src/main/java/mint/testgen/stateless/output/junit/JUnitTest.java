package mint.testgen.stateless.output.junit;

import mint.tracedata.TestIO;
import mint.tracedata.types.VariableAssignment;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JUnitTest {

    String path, testName, packageName;

    Method target;

    List<String> testCases;

    protected JUnitTest(String path, String id, String packageName, String clazz, String method) throws ClassNotFoundException {
        this.path = path;
        this.testName = id;
        ClassLoader loader = JUnitTest.class.getClassLoader();
        Class loaded = loader.loadClass(clazz);
        for(Method m : loaded.getMethods()){
            if(m.getName().equals(method)){
                target = m;
            }
        }
        this.packageName = packageName;
        testCases = new ArrayList<>();
    }

    /**
     * This will need to change. Currently assumes that variables are file names, which need to be
     * quotes as strings (specifically for the BioSUT example).
     * @param arg
     */
    protected void addTest(TestIO arg){
        String testText = target.getDeclaringClass().getName()+"."+target.getName()+"(";
        List<VariableAssignment<?>> vars = arg.getVals();
        for(int i = 0; i<vars.size();i++){
            VariableAssignment var = vars.get(i);
            testText+="\""+var.getName()+"\"";
            if(i<vars.size()-1)
                testText+=",";
        }
        testText+=");";
        testCases.add(testText);
    }

    private String constructTestString(){
        String junitString = "package "+packageName+"\nimport org.junit.Test;\n\n";
        junitString+="public class "+testName+"{\n\n";
        int counter = 0;
        for(String testInstruction: testCases) {
            junitString+="@Test\n";
            junitString+="public void "+testName+counter+"(){\n";
            junitString += testInstruction + "\n";
            junitString += "}\n\n";
            counter++;
        }
        junitString+="\n\n}";
        return junitString;
    }

    public void write() throws IOException {
        Path write = Paths.get(path + "/" + testName+".java");
        Path dir =  Paths.get(path);
        Files.createDirectories(dir);

        PrintWriter pw = new PrintWriter(write.toFile());
        pw.println(constructTestString());
        pw.close();
    }
}
