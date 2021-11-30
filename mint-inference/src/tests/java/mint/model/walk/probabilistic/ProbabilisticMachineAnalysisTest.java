package mint.model.walk.probabilistic;

import mint.model.Machine;
import mint.model.PayloadMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProbabilisticMachineAnalysisTest {

    @Test
    public void simpleEditorTest(){

        TraceDFA structure = new TraceDFA();
        Integer init = structure.getInitialState();
        Integer exit = structure.addState();
        Integer open = structure.addState();
        Integer edit = structure.addState();
        TransitionData exitElement = new TransitionData("exit", new ArrayList<>());
        TransitionData openElement = new TransitionData("open", new ArrayList<>());
        TransitionData closeElement = new TransitionData("close", new ArrayList<>());
        TransitionData editElement = new TransitionData("edit", new ArrayList<>());
        TransitionData saveElement = new TransitionData("save", new ArrayList<>());
        structure.addTransition(init,exit,exitElement);
        structure.addTransition(init,open,openElement);
        structure.addTransition(open,init,closeElement);
        structure.addTransition(open,edit,editElement);
        structure.addTransition(edit,edit,editElement);
        structure.addTransition(edit,open,saveElement);
        structure.addTransition(edit,init,closeElement);
        structure.setInitialState(init);
        Machine coreMachine = new PayloadMachine();
        coreMachine.setAutomaton(structure);

        TraceSet ts = new TraceSet();
        addSequence(new String[]{"open","edit","edit","edit","edit","save","close","exit"}, ts);
        addSequence(new String[]{"open","close","exit"}, ts);
        addSequence(new String[]{"open","edit","edit","close","exit"}, ts);
        addSequence(new String[]{"exit"}, ts);
        addSequence(new String[]{"open","edit","edit","save","close","exit"}, ts);
        addSequence(new String[]{"open","edit","save","close","open","close","exit"}, ts);

        ProbabilisticMachineAnalysis pma = new ProbabilisticMachineAnalysis(coreMachine,2,ts);

        StateEventPair sep = new StateEventPair(coreMachine.getInitialState(), exitElement.getLabel());

        List<TraceElement> prefix = new ArrayList<>();
        prefix.add(new SimpleTraceElement("save",new ArrayList<>()));
        prefix.add(new SimpleTraceElement("close",new ArrayList<>()));

        System.out.println(pma.pAGivenB(sep,prefix));
    }

    private void addSequence(String[] strings, TraceSet ts) {
        List<TraceElement> trace = new ArrayList<>();
        for(int i = 0; i<strings.length; i++){
            trace.add(new SimpleTraceElement(strings[i], new ArrayList<>()));
        }
        for(int i = 0; i<strings.length-1; i++){
            trace.get(i).setNext(trace.get(i+1));
        }
        ts.addPos(trace);
    }

    @Test
    public void listEventHashTest(){
        TraceDFA structure = new TraceDFA();
        Integer init = structure.getInitialState();
        Integer exit = structure.addState();
        Integer open = structure.addState();
        Integer edit = structure.addState();
        TransitionData exitElement = new TransitionData("exit", new ArrayList<>());
        TransitionData openElement = new TransitionData("open", new ArrayList<>());
        TransitionData closeElement = new TransitionData("close", new ArrayList<>());
        TransitionData editElement = new TransitionData("edit", new ArrayList<>());
        TransitionData saveElement = new TransitionData("save", new ArrayList<>());
        structure.addTransition(init,exit,exitElement);
        structure.addTransition(init,open,openElement);
        structure.addTransition(open,init,closeElement);
        structure.addTransition(open,edit,editElement);
        structure.addTransition(edit,edit,editElement);
        structure.addTransition(edit,open,saveElement);
        structure.addTransition(edit,init,closeElement);
        structure.setInitialState(init);
        Machine coreMachine = new PayloadMachine();
        coreMachine.setAutomaton(structure);

        TraceSet ts = new TraceSet();
        addSequence(new String[]{"open","edit","edit","edit","edit","save","close","exit"}, ts);
        addSequence(new String[]{"open","close","exit"}, ts);
        addSequence(new String[]{"open","edit","edit","close","exit"}, ts);
        addSequence(new String[]{"exit"}, ts);
        addSequence(new String[]{"open","edit","edit","save","close","exit"}, ts);
        addSequence(new String[]{"open","edit","save","close","open","close","exit"}, ts);

        ProbabilisticMachineAnalysis pma = new ProbabilisticMachineAnalysis(coreMachine,2,ts);

        StateEventPair sep = new StateEventPair(coreMachine.getInitialState(), exitElement.getLabel());

        List<TraceElement> prefix = new ArrayList<>();
        prefix.add(new SimpleTraceElement("open",new ArrayList<>()));
        prefix.add(new SimpleTraceElement("close",new ArrayList<>()));

        ListAndElement<StateEventPair> le1 = new ListAndElement<StateEventPair>(prefix,sep);

        StateEventPair sep2 = new StateEventPair(coreMachine.getInitialState(), exitElement.getLabel());

        List<TraceElement> prefix2 = new ArrayList<>();
        prefix2.add(new SimpleTraceElement("open",new ArrayList<>()));
        prefix2.add(new SimpleTraceElement("close",new ArrayList<>()));

        ListAndElement<StateEventPair> le2 = new ListAndElement<StateEventPair>(prefix2,sep2);

        Assert.assertTrue(le1.hashCode() == le2.hashCode());
        Assert.assertTrue(sep.hashCode() == sep2.hashCode());

    }

}