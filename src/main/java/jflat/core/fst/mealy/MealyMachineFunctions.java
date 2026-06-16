package core.fst.mealy;

import core.fst.moore.MooreMachineAutomata;
import core.fst.moore.MooreMachineState;
import core.fst.moore.MooreMachineTransition;

import java.util.*;

public class MealyMachineFunctions {
    public static MooreMachineAutomata convertToMoore(MealyMachineAutomata automata) {
        if (!automata.isValid()) {
            throw new IllegalArgumentException("Invalid automaton");
        }
        MooreMachineAutomata output = new MooreMachineAutomata(automata.getName() + "_Moore");

        MealyMachineState originalInitial = automata.getInitialState();
        MooreMachineState newInitial = new MooreMachineState(originalInitial.getName(), originalInitial.x, originalInitial.y, null);
        output.addState(newInitial);
        output.setInitialState(newInitial);

        Map<MealyMachineState, Map<Character, MooreMachineState>> stateMap = new HashMap<>();
        Map<Character, MooreMachineState> newInitialSplits = new HashMap<>();
        newInitialSplits.put(null, newInitial);
        stateMap.put(originalInitial, newInitialSplits);

        Queue<MealyMachineState> toProcess = new LinkedList<>();
        toProcess.add(originalInitial);

        // Pass 1 - States
        while (!toProcess.isEmpty()) {
            MealyMachineState oldCur = toProcess.poll();

            for (MealyMachineTransition t : oldCur.getTransitions()) {
                MealyMachineState oldEnd = t.getEnd();
                if (!stateMap.containsKey(t.getEnd())) {
                    stateMap.put(oldEnd, new HashMap<>());
                    toProcess.add(oldEnd);
                }
                if (!stateMap.get(t.getEnd()).containsKey(t.getOutChar())) {
                    MooreMachineState newEnd = new MooreMachineState(oldEnd.getName(), oldEnd.x, oldEnd.y, t.getOutChar());
                    stateMap.get(oldEnd).put(t.getOutChar(), newEnd);
                    output.addState(newEnd);
                }
            }
        }

        // Pass 2 - Transitions
        for (MealyMachineTransition t : automata.getTransitions()) {
            Set<MooreMachineState> possibleStarts = new HashSet<>(stateMap.get(t.getStart()).values());
            for (MooreMachineState start : possibleStarts) {
                MooreMachineState end = stateMap.get(t.getEnd()).get(t.getOutChar());
                MooreMachineTransition newTransition = new MooreMachineTransition(start, end, t.getInChar());
                output.addTransition(newTransition);
            }
        }
        output.forceDirectedLayout();
        return output;
    }

    public static String getOutput(MealyMachineAutomata automata, String input) {
        if (!automata.isValid()) {
            throw new IllegalArgumentException("Invalid automata");
        }
        return getOutput(automata.getInitialState(), input);
    }

    private static String getOutput(MealyMachineState state, String input) {
        if (input.isEmpty()) {
            return "";
        }
        for (MealyMachineTransition transition : state.getTransitions()) {
            if (transition.getInChar() == input.charAt(0)) {
                String output = getOutput(transition.getEnd(), input.substring(1));
                if (output != null) {
                    return transition.getOutChar() + output;
                }
            }
        }
        return null;
    }
}
