package jflat.core.fst.mealy;

import jflat.core.generics.AbstractAutomata;

import java.util.*;
import java.util.stream.Collectors;

public class MealyMachineAutomata extends AbstractAutomata<MealyMachineState, MealyMachineTransition> {
    public MealyMachineAutomata(String name) {
        super(name);
    }

    @Override
    public String getLatex() {
        StringBuilder sb = new StringBuilder();

        // Set of States
        List<String> stateNames = states.stream()
                .map(MealyMachineState::getName)
                .sorted()
                .toList();
        String sStr = String.join(", ", stateNames);

        // Start state
        String s0Str = getInitialState().getName();

        // In alphabet
        Set<Character> inAlphabet = getInAlphabet();
        String sigmaStr = inAlphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Out alphabet
        Set<Character> outAlphabet = getOutAlphabet();
        String lambdaStr = outAlphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Transition function
        List<String> deltaList = new ArrayList<>();
        for(MealyMachineTransition transition : transitions) {
            deltaList.add(String.format("((%s, %s), %s)",
                    transition.getStart().getName(),
                    transition.getInChar(),
                    transition.getEnd().getName()));
        }
        String deltaStr = String.join(", ", deltaList);

        // Output function
        List<String> gList = new ArrayList<>();
        for(MealyMachineTransition transition : transitions) {
            gList.add(String.format("((%s, %s), %s)",
                    transition.getStart().getName(),
                    transition.getInChar(),
                    transition.getOutChar()));
        }
        String gStr = String.join(", ", gList);

        // Final string
        sb.append("\\begin{align*}\n");
        sb.append(name).append(" &= \\langle S, s_0, \\Sigma, \\Lambda, \\delta, G \\rangle \\\\\n");
        sb.append("S &= \\{").append(sStr).append("\\} \\\\\n");
        sb.append("s_0 &= ").append(s0Str).append(" \\\\\n");
        sb.append("\\Sigma &= \\{").append(sigmaStr).append("\\} \\\\\n");
        sb.append("\\Lambda &= \\{").append(lambdaStr).append("\\} \\\\\n");
        sb.append("\\delta &= \\{").append(deltaStr).append("\\} \\\\\n");
        sb.append("G &= \\{").append(gStr).append("\\}\n");
        sb.append("\\end{align*}\n");

        return sb.toString();
    }

    public TreeSet<Character> getInAlphabet() {
        return transitions.stream()
                .map(MealyMachineTransition::getInChar)
                .collect(Collectors.toCollection (TreeSet::new));
    }

    public TreeSet<Character> getOutAlphabet() {
        return transitions.stream()
                .map(MealyMachineTransition::getOutChar)
                .collect(Collectors.toCollection (TreeSet::new));
    }

    @Override
    public boolean isValid() {
        return super.isValid() && isDeterministic();
    }

    public boolean isDeterministic() {
        for (MealyMachineState state : states) {
            Set<Character> symbols = new HashSet<>();
            for (MealyMachineTransition transition : state.getTransitions()) {
                char symbol = transition.getInChar();
                if (!symbols.add(symbol)) {
                    return false;
                }
            }
        }
        return true;
    }
}
