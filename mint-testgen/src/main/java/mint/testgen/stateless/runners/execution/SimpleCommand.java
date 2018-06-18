package mint.testgen.stateless.runners.execution;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 03/07/2017.
 */
public class SimpleCommand implements Command {

    protected List<String> command;

    public SimpleCommand(){
        command = new ArrayList<String>();
    }

    public void setCore(List<String> command){
        this.command = command;
    }


    @Override
    public List<String> getCommand() {
        return command;
    }
}
