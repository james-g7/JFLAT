package core.tm;

import core.generics.AbstractAutomata;
import core.tm.tape.Tape1D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TuringMachine1D extends AbstractAutomata<TuringMachineState1D, TuringMachineTransition1D> {
    protected Tape1D<Character> tape = new Tape1D<>();

    public TuringMachine1D(String name) {
        super(name);
    }

    @Override
    public String getLatex() {
        StringBuilder sb = new StringBuilder();

        // Set of States
        List<String> stateNames = states.stream()
                .map(TuringMachineState1D::getName)
                .sorted()
                .toList();
        String qStr = String.join(", ", stateNames);

        // Start state
        String q0Str = getInitialState().getName();

        // Final states
        List<String> finalStates = states.stream()
                .filter(TuringMachineState1D::isFinal)
                .map(TuringMachineState1D::getName)
                .sorted()
                .toList();
        String fStr = String.join(", ", finalStates);

        // Input alphabet
        TreeSet<Character> inAlphabet = getInAlphabet();
        String sigmaStr = inAlphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Tape alphabet
        TreeSet<Character> tapeAlphabet = getTapeAlphabet();
        String gammaStr = tapeAlphabet.stream()
                .map(c -> c == null ? "\\sqcup" : String.valueOf(c))
                .collect(Collectors.joining(", "));

        // Transition function
        List<String> deltaList = new ArrayList<>();
        for(TuringMachineTransition1D transition : transitions) {
            String readSym = transition.getRead() == null ? "\\sqcup" : String.valueOf(transition.getRead());
            String writeSym = transition.getWrite() == null ? "\\sqcup" : String.valueOf(transition.getWrite());

            deltaList.add(String.format("((%s, %s), (%s, %s, \\text{%s}))",
                    transition.getStart().getName(),
                    readSym,
                    transition.getEnd().getName(),
                    writeSym,
                    transition.getDestination()));
        }
        String deltaStr = String.join(", ", deltaList);

        // Final string construction
        sb.append("\\begin{align*}\n");
        sb.append(name).append(" &= \\langle Q, \\Sigma, \\Gamma, \\delta, q_0, \\sqcup, F \\rangle \\\\\n");
        sb.append("Q &= \\{").append(qStr).append("\\} \\\\\n");
        sb.append("\\Sigma &= \\{").append(sigmaStr).append("\\} \\\\\n");
        sb.append("\\Gamma &= \\{").append(gammaStr).append("\\} \\\\\n");
        sb.append("\\delta &= \\{").append(deltaStr).append("\\} \\\\\n");
        sb.append("q_0 &= ").append(q0Str).append(" \\\\\n");
        sb.append("F &= \\{").append(fStr).append("\\}\n");
        sb.append("\\end{align*}\n");

        return sb.toString();
    }

    public TreeSet<Character> getInAlphabet() {
        return transitions.stream()
                .map(TuringMachineTransition1D::getRead)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public TreeSet<Character> getTapeAlphabet() {
        return transitions.stream()
                .map(TuringMachineTransition1D::getWrite)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
