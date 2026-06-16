package core.fst.moore;

import core.fst.mealy.MealyMachineAutomata;
import core.fst.mealy.MealyMachineState;
import core.fst.mealy.MealyMachineTransition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MooreMachineFunctions {
    public static MealyMachineAutomata convertToMealy(MooreMachineAutomata automata) {
        if (!automata.isValid()) {
            throw new IllegalArgumentException("Invalid automaton");
        }
        MealyMachineAutomata output = new MealyMachineAutomata(automata.getName() + "_Mealy");

        MooreMachineState originalInitial = automata.getInitialState();
        MealyMachineState newInitial = new MealyMachineState(originalInitial.getName(), originalInitial.x, originalInitial.y);
        output.addState(newInitial);
        output.setInitialState(newInitial);

        Map<MooreMachineState, MealyMachineState> stateMap = new HashMap<>();
        stateMap.put(originalInitial, newInitial);

        Queue<MooreMachineState> toProcess = new LinkedList<>();
        toProcess.add(originalInitial);

        while (!toProcess.isEmpty()) {
            MooreMachineState oldCur = toProcess.poll();
            MealyMachineState newCur = stateMap.get(oldCur);

            for (MooreMachineTransition t : oldCur.getTransitions()) {
                if (!stateMap.containsKey(t.getEnd())) {
                    MooreMachineState oldEnd = t.getEnd();
                    MealyMachineState newEnd = new MealyMachineState(oldEnd.getName(), oldEnd.x, oldEnd.y);
                    stateMap.put(oldEnd, newEnd);
                    toProcess.add(oldEnd);
                    output.addState(newEnd);
                }
                MealyMachineTransition newTransition = new MealyMachineTransition(newCur, stateMap.get(t.getEnd()), t.getSymbol(), t.getEnd().getOutput());
                output.addTransition(newTransition);
            }
        }
        output.forceDirectedLayout();
        return output;
    }

    public static String getOutput(MooreMachineAutomata automata, String input) {
        if (!automata.isValid()) {
            throw new IllegalArgumentException("Invalid automata");
        }
        return getOutput(automata.getInitialState(), input);
    }

    private static String getOutput(MooreMachineState state, String input) {
        if (input.isEmpty()) {
            return "";
        }
        for(MooreMachineTransition transition : state.getTransitions()) {
            if (transition.getSymbol() == input.charAt(0)) {
                return transition.getEnd().getOutput() + getOutput(transition.getEnd(), input.substring(1));
            }
        }
        return null;
    }
}
