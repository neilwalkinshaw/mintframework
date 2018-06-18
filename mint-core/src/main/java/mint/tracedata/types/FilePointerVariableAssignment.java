package mint.tracedata.types;

import java.io.File;

/**
 *
 * VariableAssignment representing a pointer to a file on the system (pointer is represented as a string).
 * Intended for use as an input for testing.
 *
 * Created by neilwalkinshaw on 03/09/2014.
 */
public class FilePointerVariableAssignment extends StringVariableAssignment {

    public FilePointerVariableAssignment(String name) {
        super(name);
    }

    public FilePointerVariableAssignment(String name, String value) {
        super(name,value);
    }

    @Override
    public VariableAssignment<?> createNew(String name, String value) {
        FilePointerVariableAssignment sva = new FilePointerVariableAssignment(name);
        sva.setParameter(isParameter());
        if(value == null)
            setNull(true);
        else if(value.trim().equals("*"))
            setNull(true);
        else
            sva.setStringValue(value);
        return sva;
    }

    @Override
    public VariableAssignment<String> copy() {

        FilePointerVariableAssignment copied = new FilePointerVariableAssignment(name,value);
        copied.setParameter(isParameter());
        return copied;
    }

    @Override
    protected String generateRandom() {
       File redirectFile = new File(value);
        assert(redirectFile.isDirectory());
        File[] files = redirectFile.listFiles();
        return files[rand.nextInt(files.length)].getAbsolutePath();

    }

}
