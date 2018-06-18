package mint.testgen.stateless.runners.execution;

import java.util.List;

/**
 * Created by neilwalkinshaw on 03/07/2017.
 */
public class CommandDecorator implements Command {

    Command decorated;

    public CommandDecorator(Command toBeDecorated){
        decorated = toBeDecorated;
    }



    @Override
    public List<String> getCommand() {
        return decorated.getCommand();
    }

    @Override
    public void setCore(List<String> command) {
        decorated.setCore(command);
    }

}
