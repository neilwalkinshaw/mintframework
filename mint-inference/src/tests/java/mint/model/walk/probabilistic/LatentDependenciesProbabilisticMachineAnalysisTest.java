package mint.model.walk.probabilistic;

import mint.model.LatentProbabilitiesMachine;
import mint.model.dfa.TraceDFA;
import mint.model.dfa.TransitionData;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.types.VariableAssignment;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class LatentDependenciesProbabilisticMachineAnalysisTest {

    @Test
    public void walkConditionalProbability() {

        LatentProbabilitiesMachine lpm = new LatentProbabilitiesMachine();
        TraceDFA<Double> automaton = new TraceDFA<>();
        Integer A = automaton.addState();
        automaton.setAccept(A, TraceDFA.Accept.ACCEPT);
        automaton.setInitialState(A);
        Integer B = automaton.addState();
        automaton.setAccept(B, TraceDFA.Accept.ACCEPT);
        Integer C = automaton.addState();
        automaton.setAccept(C, TraceDFA.Accept.ACCEPT);
        Integer D = automaton.addState();
        automaton.setAccept(D, TraceDFA.Accept.ACCEPT);
        automaton.addTransition(A,B,new TransitionData<>("submit_password",0.62));
        automaton.addTransition(A,C,new TransitionData<>("request_reminder",0.38));
        automaton.addTransition(B,A,new TransitionData<>("fail",0.5));
        automaton.addTransition(B,D,new TransitionData<>("success",0.5));
        automaton.addTransition(C,A,new TransitionData<>("receive_reminder",1D));
        lpm.setAutomaton(automaton);
        lpm.addLatentDependency(B,"fail",A,"request_reminder",0.6);
        lpm.addLatentDependency(C,"receive_reminder",A,"submit_password",0.9);

        LatentDependenciesProbabilisticMachineAnalysis ldpma = new LatentDependenciesProbabilisticMachineAnalysis(lpm);
        List<TraceElement> check = new ArrayList<>();
        TraceElement submitPassword =  new SimpleTraceElement("submit_password", new HashSet<>());
        TraceElement fail = new SimpleTraceElement("fail", new HashSet<>());
        submitPassword.setNext(fail);
        TraceElement request_reminder = new SimpleTraceElement("request_reminder", new HashSet<>());
        fail.setNext(request_reminder);
        TraceElement receive_reminder = new SimpleTraceElement("receive_reminder", new HashSet<>());
        request_reminder.setNext(receive_reminder);
        TraceElement submit_password = new SimpleTraceElement("submit_password", new HashSet<>());
        receive_reminder.setNext(submit_password);
        TraceElement success = new SimpleTraceElement("success", new HashSet<>());
        submit_password.setNext(success);
        check.add(submit_password);
        check.add(fail);
        check.add(request_reminder);
        check.add(receive_reminder);
        check.add(submit_password);
        check.add(success);

        double prob = ldpma.walkConditionalProbability(check);
        System.out.println(prob);

        double expected = calculateExpected();

        Assert.assertEquals(expected,prob, 0.00000001);

    }

    private double calculateExpected() {
        double result = 1;
        result = result * 0.62 * 0.5 * 0.6 * 0.9 * 1 * 0.5;
        return result;
    }
}