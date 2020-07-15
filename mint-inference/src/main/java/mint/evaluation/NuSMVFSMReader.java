package mint.evaluation;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TransitionData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * Parses NuSMV FSM representations.
 * Assumes initial state is s0.
 */

public class NuSMVFSMReader {

    Machine dfa = new PayloadMachine();

    public Machine getMachine(){
        return dfa;
    }

    public void readFile(File target){

        try {
            BufferedReader br = new BufferedReader(new FileReader(target));
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if(line.startsWith("VAR")){
                    parseStates(line);
                }
                if(line.startsWith("state"))
                    parseTransition(line);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    private void parseTransition(String line) {
        //Assumes format: "state = s0 & inp = CH_CLOSE: s0;"
        String[] spaceSplit = line.split(" ");
        String sourceState = spaceSplit[2].trim().substring(1);
        String label = spaceSplit[6].trim().substring(0,spaceSplit[6].trim().length()-1);
        String destinationState = spaceSplit[7].trim().substring(0,spaceSplit[7].trim().length()-1);
        if(destinationState.startsWith("s")){
            destinationState = destinationState.substring(1);
            TransitionData<String> data = new TransitionData(label,label);
            Integer source = Integer.parseInt(sourceState);
            Integer destination = Integer.parseInt(destinationState);
            dfa.getAutomaton().addTransition(source,destination,data);
        }
    }

    protected void parseStates(String line) {
        line = line.substring(line.indexOf('{')+1, line.indexOf('}'));
        String[] tokenized = line.split(",");
        for(int i = 0; i< tokenized.length; i++){
            Integer parsed = Integer.parseInt(tokenized[i].substring(1));
            dfa.getAutomaton().addState(parsed);
        }
    }

}

