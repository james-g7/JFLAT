package core.fsa;

import core.generics.AbstractAutomata;

import java.util.*;
import java.util.stream.Collectors;

public class FSAAutomata extends AbstractAutomata<FSAState, FSATransition> {

    public FSAAutomata(String name) {
        super(name);
    }

    public boolean isDeterministic() {
        for (FSAState state : states) {
            Set<Character> symbols = new HashSet<>();
            for (FSATransition transition : state.getTransitions()) {
                Character symbol = transition.getSymbol();

                if (symbol == null) {
                    return false;
                }

                if (!symbols.add(symbol)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getLatex() {
        StringBuilder sb = new StringBuilder();

        // Set of States
        List<String> stateNames = states.stream()
                .map(FSAState::getName)
                .sorted()
                .toList();
        String qStr = String.join(", ", stateNames);

        // Alphabet
        Set<Character> alphabet = getAlphabet();
        String sigmaStr = alphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        // Transitions
        List<String> deltaList = new ArrayList<>();
        for(FSATransition transition : transitions) {
                deltaList.add(String.format("δ(%s, %s) = %s",
                        transition.getStart().getName(),
                        transition.getSymbol() == null ? "λ" : transition.getSymbol(),
                        transition.getEnd().getName()));
        }
        String deltaStr = String.join(", ", deltaList);

        // Initial State
        FSAState startState = getInitialState();
        String iStr = startState != null ? startState.getName() : "None";

        // Final States
        List<String> finalStates = getFinalStates().stream()
                .map(FSAState::getName)
                .sorted()
                .toList();
        String fStr = String.join(", ", finalStates);

        // Final string
        sb.append(name).append(" = ⟨Q, Σ, δ, i, F⟩\n");
        sb.append("Q = {").append(qStr).append("}\n");
        sb.append("Σ = {").append(sigmaStr).append("}\n");
        sb.append("δ: ").append(deltaStr).append("\n");
        sb.append("i = ").append(iStr).append("\n");
        sb.append("F = {").append(fStr).append("}\n");
        return sb.toString();
    }
    
    public TreeSet<Character> getAlphabet() {
        return transitions.stream()
                .map(FSATransition::getSymbol)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<FSAState> getFinalStates() {
        return states.stream()
                .filter(FSAState::isFinal)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<FSAState> getReachableStates() {
        Set<FSAState> reachable = new HashSet<>();
        Queue<FSAState> queue = new LinkedList<>();

        FSAState startState = getInitialState();
        if (startState != null) {
            queue.add(startState);
            reachable.add(startState);
        }

        while (!queue.isEmpty()) {
            FSAState current = queue.poll();
            for (FSATransition t : transitions) {
                if (t.getStart().equals(current) && !reachable.contains(t.getEnd())) {
                    reachable.add(t.getEnd());
                    queue.add(t.getEnd());
                }
            }
        }
        return reachable;
    }

    // 2. Backward BFS to find states that can reach a Final State
    private Set<FSAState> getProductiveStates() {
        Set<FSAState> productive = new HashSet<>();
        Queue<FSAState> queue = new LinkedList<>();

        // Add all final states to the queue first
        for (FSAState s : states) {
            if (s.isFinal()) { // Assuming State has an isFinal() getter
                queue.add(s);
                productive.add(s);
            }
        }

        // Traverse transitions backwards
        while (!queue.isEmpty()) {
            FSAState current = queue.poll();
            for (FSATransition t : transitions) {
                if (t.getEnd().equals(current) && !productive.contains(t.getStart())) {
                    productive.add(t.getStart());
                    queue.add(t.getStart());
                }
            }
        }
        return productive;
    }

    @Override
    public void removeUselessStates() {
        Set<FSAState> reachableStates = getReachableStates();
        Set<FSAState> productiveStates = getProductiveStates();

        List<FSAState> removal = new LinkedList<>();
        for (FSAState s : states) {
            // A state is useless if it cannot be reached from the start,
            // OR if it cannot reach a final state.
            if (!reachableStates.contains(s) || !productiveStates.contains(s)) {
                removal.add(s);
            }
        }

        removal.forEach(this::removeState);
    }
}