package core.pda.emptyStack;

import core.generics.AbstractAutomata;

import java.util.*;
import java.util.stream.Collectors;

public class EmptyStackPushdownAutomata extends AbstractAutomata<EmptyStackPushdownState, EmptyStackPushdownTransition> {

    private final Stack<String> stack = new Stack<>();

    public EmptyStackPushdownAutomata(String name) {
        super(name);
        stack.push("empty");
    }

    @Override
    public String getLatex() {
        StringBuilder sb = new StringBuilder();

        // Set of States
        List<String> stateNames = states.stream()
                .map(EmptyStackPushdownState::getName)
                .sorted()
                .toList();
        String qStr = String.join(", ", stateNames);

        // Start state
        String q0Str = getInitialState().getName();

        // Input alphabet
        TreeSet<Character> inAlphabet = getInAlphabet();
        String sigmaStr = inAlphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Stack alphabet
        TreeSet<String> stackAlphabet = getStackAlphabet();
        String gammaStr = stackAlphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Transition function
        List<String> deltaList = new ArrayList<>();
        for(EmptyStackPushdownTransition transition : transitions) {

            // Read Symbol
            String readSym = transition.getInSymbol() == null
                    ? "\\lambda"
                    : String.valueOf(transition.getInSymbol());

            // Pop Symbol
            String popRaw = transition.getPopSymbol() == null
                    ? null
                    : String.valueOf(transition.getPopSymbol());

            String popSym = (popRaw == null) ? "\\lambda"
                    : popRaw.equals("empty") ? "Z_0"
                      : popRaw;

            // Push Symbols
            String pushRaw = transition.getPushSymbols();
            String pushSym = (pushRaw == null || pushRaw.isEmpty())
                    ? "\\lambda"
                    : pushRaw.replace("empty", "Z_0");

            deltaList.add(String.format("((%s, %s, %s), (%s, %s))",
                    transition.getStart().getName(),
                    readSym,
                    popSym,
                    transition.getEnd().getName(),
                    pushSym));
        }
        String deltaStr = String.join(", ", deltaList);

        // Final string
        sb.append("\\begin{align*}\n");
        sb.append(name).append(" &= \\langle Q, \\Sigma, \\Gamma, \\delta, q_0, Z_0 \\rangle \\\\\n");
        sb.append("Q &= \\{").append(qStr).append("\\} \\\\\n");
        sb.append("\\Sigma &= \\{").append(sigmaStr).append("\\} \\\\\n");
        sb.append("\\Gamma &= \\{").append(gammaStr).append("\\} \\\\\n");
        sb.append("\\delta &= \\{").append(deltaStr).append("\\} \\\\\n");
        sb.append("q_0 &= ").append(q0Str).append(" \\\\\n");
        sb.append("\\end{align*}\n");

        return sb.toString();
    }

    public String pop() {
        return stack.pop();
    }

    public void push(String c) {
        stack.push(c);
    }

    public TreeSet<String> getStackAlphabet() {
        TreeSet<String> stackAlphabet = new TreeSet<>();
        stackAlphabet.add("Z_0");

        for (EmptyStackPushdownTransition t : transitions) {
            if (t.getPopSymbol() != null) {
                stackAlphabet.add(t.getPopSymbol());
            }
            String pushStr = t.getPushSymbols();
            if (pushStr != null && !pushStr.isEmpty()) {
                String cleanPush = pushStr.replace("empty", "");
                for (char c : cleanPush.toCharArray()) {
                    stackAlphabet.add(String.valueOf(c));
                }
            }
        }

        return stackAlphabet;
    }

    public TreeSet<Character> getInAlphabet() {
        return transitions.stream()
                .map(EmptyStackPushdownTransition::getInSymbol)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
