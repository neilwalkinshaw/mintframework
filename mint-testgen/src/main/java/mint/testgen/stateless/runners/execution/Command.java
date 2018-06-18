package mint.testgen.stateless.runners.execution;


import java.util.List;

/**
 * Created by neilwalkinshaw on 03/07/2017.
 */
public interface Command{

    List<String> getCommand();

    void setCore(List<String> command);

}
