package jflat.core.fst.moore;

import jflat.core.generics.AbstractAutomata;

import java.util.*;
import java.util.stream.Collectors;

public class MooreMachineAutomata extends AbstractAutomata<MooreMachineState, MooreMachineTransition> {
    public MooreMachineAutomata(String name) {
        super(name);
    }

    @Override
    public String getLatex() {
        StringBuilder sb = new StringBuilder();

        // Set of States
        List<String> stateNames = states.stream()
                .map(MooreMachineState::getName)
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
        for(MooreMachineTransition transition : transitions) {
            deltaList.add(String.format("((%s, %s), %s)",
                    transition.getStart().getName(),
                    transition.getSymbol(),
                    transition.getEnd().getName()));
        }
        String deltaStr = String.join(", ", deltaList);

        // Output function
        List<String> gList = new ArrayList<>();
        for(MooreMachineState state : states) {
            gList.add(String.format("(%s, %s)",
                    state.getName(),
                    state.getOutput()));
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
                .map(MooreMachineTransition::getSymbol)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public TreeSet<Character> getOutAlphabet() {
        return states.stream()
                .map(MooreMachineState::getOutput)
                .collect(Collectors.toCollection (TreeSet::new));
    }

    @Override
    public boolean isValid() {
        return super.isValid() && isDeterministic();
    }

    public boolean isDeterministic() {
        for (MooreMachineState state : states) {
            Set<Character> symbols = new HashSet<>();
            for (MooreMachineTransition transition : state.getTransitions()) {
                char symbol = transition.getSymbol();
                if (!symbols.add(symbol)) {
                    return false;
                }
            }
        }
        return true;
    }

}
