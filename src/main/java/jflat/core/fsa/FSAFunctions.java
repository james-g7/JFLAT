package core.fsa;

import java.util.*;
import java.util.stream.Collectors;

public class FSAFunctions {
    public static FSAAutomata convertToDFA(FSAAutomata automata) {
        if(!automata.isValid()) {
            throw new IllegalArgumentException("Automata is not valid.");
        }
        if(automata.isDeterministic()) {
            FSAAutomata output = deepCopy(automata);
            output.setName(automata.getName() + "_DFA");
            return output;
        }

        FSAAutomata output = new FSAAutomata(automata.getName() + "_DFA");
        Set<Character> alphabet = new TreeSet<>(automata.getAlphabet());

        Map<Set<FSAState>, FSAState> combinedStates = new HashMap<>();
        Queue<Set<FSAState>> queue = new LinkedList<>();

        Set<FSAState> initialSet = lambdaClosure(Collections.singleton(automata.getInitialState()));
        queue.add(initialSet);

        FSAState initialDFAState = new FSAState(
                initialSet.stream().anyMatch(FSAState::isFinal),
                initialSet.stream().map(FSAState::getName).sorted().collect(Collectors.joining(",")),
                (int) initialSet.stream().mapToInt(s -> s.x).average().orElse(0),
                (int) initialSet.stream().mapToInt(s -> s.y).average().orElse(0)
        );
        combinedStates.put(initialSet, initialDFAState);
        output.addState(initialDFAState);
        output.setInitialState(initialDFAState);

        while (!queue.isEmpty()) {
            Set<FSAState> currentSet = queue.poll();
            FSAState currentDFAState = combinedStates.get(currentSet);

            for (Character c : alphabet) {
                Set<FSAState> moveSet = new HashSet<>();

                for (FSAState s : currentSet) {
                    for (FSATransition t : s.getTransitions()) {
                        if (t.getSymbol() != null && t.getSymbol().equals(c)) {
                            moveSet.add(t.getEnd());
                        }
                    }
                }
                Set<FSAState> closureSet = lambdaClosure(moveSet);
                if (closureSet.isEmpty()) {
                    continue;
                }

                if (!combinedStates.containsKey(closureSet)) {
                    FSAState newDFAState = new FSAState(
                            closureSet.stream().anyMatch(FSAState::isFinal),
                            closureSet.stream().map(FSAState::getName).sorted().collect(Collectors.joining(",")),
                            (int) closureSet.stream().mapToInt(s -> s.x).average().orElse(0),
                            (int) closureSet.stream().mapToInt(s -> s.y).average().orElse(0)
                    );
                    combinedStates.put(closureSet, newDFAState);
                    output.addState(newDFAState);
                    queue.add(closureSet);
                }
                FSATransition t = new FSATransition(currentDFAState, combinedStates.get(closureSet), c);
                output.addTransition(t);
            }
        }
        return output;
    }

    private static Set<FSAState> lambdaClosure(Set<FSAState> startStates) {
        Set<FSAState> closure = new HashSet<>(startStates);
        Queue<FSAState> queue = new LinkedList<>(startStates);

        while (!queue.isEmpty()) {
            FSAState current = queue.poll();
            for (FSATransition t : current.getTransitions()) {
                if (t.getSymbol() == null) {
                    FSAState next = t.getEnd();
                    if (!closure.contains(next)) {
                        closure.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return closure;
    }

    public static FSAAutomata getComplement(FSAAutomata automata) {
        if(!automata.isDeterministic() || !automata.isValid()) {
            throw new IllegalArgumentException();
        }

        FSAAutomata output = deepCopy(automata);
        output.setName(automata.getName() + "_complement");
        for(FSAState state : automata.getStates()) {
            state.setFinal(!state.isFinal());
        }
        return output;
    }

    public static FSAAutomata getUnion(List<FSAAutomata> automatas) {
        if (automatas.size() < 2 || automatas.stream().anyMatch(automata -> !automata.isValid())) {
            throw new IllegalArgumentException();
        }


        String name = automatas.stream()
                .map(FSAAutomata::getName)
                .collect(Collectors.joining("_union_"));
        FSAAutomata output = new FSAAutomata(name);

        FSAState initialState = new FSAState(false, "initial", 0, 0);
        output.addState(initialState);
        output.setInitialState(initialState);

        for (FSAAutomata a : automatas) {
            Map<FSAState, FSAState> mappings = new HashMap<>();
            for(FSAState s : a.getStates()) {
                FSAState temp = new FSAState(s.isFinal(), a.getName() + "_" + s.getName(), s.x, s.y);
                output.addState(temp);
                mappings.put(s, temp);
                if(a.getInitialState().equals(s)) {
                    FSATransition transition = new FSATransition(initialState, temp, null);
                    output.addTransition(transition);
                }
            }
            for(FSATransition t : a.getTransitions()) {
                FSATransition transition = new FSATransition(mappings.get(t.getStart()), mappings.get(t.getEnd()), t.getSymbol());
                output.addTransition(transition);
            }
        }
        return output;
    }

    public static FSAAutomata getIntersection(List<FSAAutomata> automatas) {
        if (automatas.size() < 2 || automatas.stream().anyMatch(automata -> !automata.isValid())) {
            throw new IllegalArgumentException();
        }

        String name = automatas.stream()
                .map(FSAAutomata::getName)
                .collect(Collectors.joining("_union_"));
        FSAAutomata output = new FSAAutomata(name);

        Map<FSAState, FSAAutomata> ownerByState = new HashMap<>();
        for (FSAAutomata automata : automatas) {
            for (FSAState state : automata.getStates()) {
                ownerByState.put(state, automata);
            }
        }

        Set<Set<FSAState>> prevSeenSets = new HashSet<>();
        Set<Set<FSAState>> curSeenSets = new HashSet<>();
        Map<Set<FSAState>, FSAState> mapping = new HashMap<>();
        Set<Character> alphabet = automatas.stream()
                .map(FSAAutomata::getAlphabet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Set<FSAState> initialStates = automatas.stream()
                .map(FSAAutomata::getInitialState)
                .collect(Collectors.toSet());
        FSAState initial = new FSAState(
                (boolean) initialStates.stream().allMatch(FSAState::isFinal),
                (String) initialStates.stream()
                        .map(s -> ownerByState.get(s).getName() + "_" + s.getName())
                        .collect(Collectors.joining(",")),
                (int) initialStates.stream()
                        .mapToInt(s -> s.x)
                        .average()
                        .orElse(0),
                (int) initialStates.stream()
                        .mapToInt(s -> s.y)
                        .average()
                        .orElse(0)
        );
        output.addState(initial);
        output.setInitialState(initial);
        mapping.put(initialStates, initial);
        curSeenSets.add(initialStates);

        while(curSeenSets.size() != prevSeenSets.size()) {
            Set<Set<FSAState>> unseenSets = new HashSet<>(curSeenSets);
            unseenSets.removeAll(prevSeenSets);
            prevSeenSets = new HashSet<>(curSeenSets);

            for(Set<FSAState> newSet : unseenSets) {
                for (Character c : alphabet) {
                    Set<FSAState> destinations = new HashSet<>();
                    for (FSAState s : newSet) {
                        destinations.add(
                                s.getTransitions().stream()
                                        .filter(t -> t.getSymbol().equals(c))
                                        .map(FSATransition::getEnd)
                                        .findAny()
                                        .orElseThrow()
                        );
                    }
                    if (!mapping.containsKey(destinations)) {
                        FSAState temp = new FSAState(
                                (boolean) destinations.stream().allMatch(FSAState::isFinal),
                                (String) destinations.stream()
                                        .map(s -> ownerByState.get(s).getName() + "_" + s.getName())
                                        .collect(Collectors.joining(",")),
                                (int) destinations.stream()
                                        .mapToInt(s -> s.x)
                                        .average()
                                        .orElse(0),
                                (int) destinations.stream()
                                        .mapToInt(s -> s.y)
                                        .average()
                                        .orElse(0)
                        );
                        output.addState(temp);
                        mapping.put(destinations, temp);
                        curSeenSets.add(destinations);
                    }
                    FSATransition transition = new FSATransition(mapping.get(newSet), mapping.get(destinations), c);
                    output.addTransition(transition);
                }
            }
        }
        return output;
    }

    public static FSAAutomata getConcatenation(List<FSAAutomata> automatas) {
        if (automatas.size() < 2 || automatas.stream().anyMatch(automata -> !automata.isValid())) {
            throw new IllegalArgumentException();
        }


        String name = automatas.stream()
                .map(FSAAutomata::getName)
                .collect(Collectors.joining("_union_"));
        FSAAutomata output = new FSAAutomata(name);

        List<FSAState> previousFinals = new LinkedList<>();
        for (int i = 0; i < automatas.size(); i+=1) {
            FSAAutomata a = automatas.get(i);
            Map<FSAState, FSAState> mappings = new HashMap<>();


            if(i == 0) {
                FSAState initialState = a.getInitialState();
                FSAState initialStateCopy = new FSAState(false, a.getName() + "_" + initialState.getName(), initialState.x, initialState.y);
                output.addState(initialStateCopy);
                output.setInitialState(initialStateCopy);
                mappings.put(initialState, initialStateCopy);
            }

            for(FSAState s : a.getStates()) {
                if(a.getInitialState().equals(s) && i == 0) {
                    continue;
                }
                FSAState temp;
                if (i == automatas.size() - 1) {
                    temp = new FSAState(s.isFinal(), a.getName() + "_" + s.getName(), s.x, s.y);
                } else {
                    temp = new FSAState(false, a.getName() + "_" + s.getName(), s.x, s.y);
                }
                output.addState(temp);
                mappings.put(s, temp);
                if(s.isFinal()) {
                    previousFinals.add(temp);
                }


                if(a.getInitialState().equals(s)) {
                    for(FSAState finalState : previousFinals) {
                        FSATransition transition = new FSATransition(finalState, temp, null);
                        output.addTransition(transition);
                    }
                    previousFinals.clear();
                }
            }
            for(FSATransition t : a.getTransitions()) {
                FSATransition transition = new FSATransition(mappings.get(t.getStart()), mappings.get(t.getEnd()), t.getSymbol());
                output.addTransition(transition);
            }
        }
        return output;

    }

    public static FSAAutomata getKleeneStar(FSAAutomata automata) {
        if(!automata.isValid()) {
            throw new IllegalArgumentException();
        }

        FSAAutomata output = deepCopy(automata);
        output.setName(automata.getName() + "_kleene_star");

        FSAState originalInitial = output.getInitialState();
        FSAState initial = new FSAState(true, output.generateUniqueStateName("initial"), 0, 0);
        output.addState(initial);
        output.setInitialState(initial);

        for(FSAState state : output.getStates()) {
            if(state.isFinal()) {
                FSATransition transition = new FSATransition(state, originalInitial, null);
                output.addTransition(transition);
            }
        }
        return output;
    }

    public static FSAAutomata getReversal(FSAAutomata automata) {
        if(!automata.isValid()) {
            throw new IllegalArgumentException();
        }

        FSAAutomata output = new FSAAutomata(automata.getName() + "_reversal");
        FSAState start = new FSAState(false, automata.generateUniqueStateName("initial"), 0 , 0);
        output.addState(start);
        output.setInitialState(start);

        Map<FSAState, FSAState> stateMap = new HashMap<>();
        for (FSAState s : automata.getStates()) {
            FSAState copy = new FSAState(automata.getInitialState().equals(s), s.getName(), s.x, s.y);
            output.addState(copy);
            stateMap.put(s, copy);
            if (s.isFinal()) {
                FSATransition transition = new FSATransition(start, copy, null);
                output.addTransition(transition);
            }
        }
        for (FSATransition t : automata.getTransitions()) {
            FSATransition transition = new FSATransition(stateMap.get(t.getEnd()), stateMap.get(t.getStart()), t.getSymbol());
            output.addTransition(transition);
        }

        return output;
    }

    public static boolean equivalent(FSAAutomata automataA, FSAAutomata automataB) {
        if(!automataA.isDeterministic() || !automataA.isValid()) {
            throw new IllegalArgumentException();
        }
        if(!automataB.isDeterministic() || !automataB.isValid()) {
            throw new IllegalArgumentException();
        }

        FSAAutomata aComplement = getComplement(deepCopy(automataA));
        FSAAutomata bComplement = getComplement(deepCopy(automataB));
        List<FSAAutomata> automataList = new LinkedList<>();
        automataList.add(getIntersection(List.of(automataA, bComplement)));
        automataList.add(getIntersection(List.of(aComplement, automataB)));

        return acceptsAString(getUnion(automataList));
    }

    public static boolean acceptsAString(FSAAutomata automata) {
        if(!automata.isValid()) {
            throw new IllegalArgumentException();
        }

        return automata.getReachableStates().stream().anyMatch(FSAState::isFinal);
    }

    public static FSAAutomata minimise(FSAAutomata automata) {
        if(!automata.isDeterministic() || !automata.isValid()) {
            throw new IllegalArgumentException();
        }
        FSAAutomata completeAutomata = getComplete(deepCopy(automata));

        List<Set<FSAState>> partCur = new LinkedList<>();

        Set<FSAState> finalStates = completeAutomata.getFinalStates();
        if (!finalStates.isEmpty()) {
            partCur.add(finalStates);
        }

        Set<FSAState> nonFinalStates = new HashSet<>(completeAutomata.getStates());
        nonFinalStates.removeAll(finalStates);
        if (!nonFinalStates.isEmpty()) {
            partCur.add(nonFinalStates);
        }

        Set<Character> alphabet = new TreeSet<>(completeAutomata.getAlphabet());
        List<Set<FSAState>> partPrev = new LinkedList<>();
        while (!partPrev.equals(partCur)) {
            partPrev = partCur;
            partCur = new ArrayList<>();

            for (Set<FSAState> part : partPrev) {
                if (part.isEmpty()) {
                    continue;
                }

                Map<Map<Character, Integer>, Set<FSAState>> distinguishedSets = new HashMap<>();
                for (FSAState s : part) {
                    // The "signature" maps a character to the partition index of its destination
                    Map<Character, Integer> stateSignature = getStateSignature(s, partPrev);

                    // Group states by their signature
                    distinguishedSets.computeIfAbsent(stateSignature, k -> new HashSet<>()).add(s);
                }

                partCur.addAll(distinguishedSets.values());
            }
        }

        FSAAutomata output = new FSAAutomata(automata.getName() + "_minimised");
        Map<Set<FSAState>, FSAState> combinedStates = new HashMap<>();
        for (Set<FSAState> part : partCur) {
            if (part.isEmpty()) {
                continue;
            }

            FSAState representative = part.iterator().next();

            FSAState temp = new FSAState(
                    representative.isFinal(),
                    part.stream()
                            .map(FSAState::getName)
                            .collect(Collectors.joining(",")),
                    (int) part.stream()
                            .mapToInt(s -> s.x)
                            .average()
                            .orElse(0),
                    (int) part.stream()
                            .mapToInt(s -> s.y)
                            .average()
                            .orElse(0)
            );

            combinedStates.put(part, temp);
            output.addState(temp);
            if (part.contains(completeAutomata.getInitialState())) {
                output.setInitialState(temp);
            }
        }

        for (Set<FSAState> part : partCur) {
            if (part.isEmpty()) {
                continue;
            }

            FSAState representative = part.iterator().next();
            for (Character c : alphabet) {
                FSAState endRepresentative = representative.getTransitions().stream()
                        .filter(t -> t.getSymbol().equals(c))
                        .map(FSATransition::getEnd)
                        .findAny()
                        .orElseThrow();

                FSAState end = null;
                for (Set<FSAState> parts: combinedStates.keySet()) {
                    if(parts.contains(endRepresentative)) {
                        end = combinedStates.get(parts);
                        break;
                    }
                }

                if (end == null) {
                    throw new IllegalStateException("Could not find destination partition during minimisation.");
                }
                FSATransition transition = new FSATransition(combinedStates.get(part), end, c);
                output.addTransition(transition);
            }
        }

        return output;
    }

    private static Map<Character, Integer> getStateSignature(FSAState s, List<Set<FSAState>> partPrev) {
        Map<Character, Integer> stateSignature = new HashMap<>();

        for (FSATransition t : s.getTransitions()) {
            Character c = t.getSymbol();
            FSAState destination = t.getEnd();

            // Find which partition index the destination belongs to
            int partitionIndex = -1;
            for (int i = 0; i < partPrev.size(); i++) {
                if (partPrev.get(i).contains(destination)) {
                    partitionIndex = i;
                    break;
                }
            }
            stateSignature.put(c, partitionIndex);
        }
        return stateSignature;
    }

    public static FSAAutomata getComplete(FSAAutomata automata) {
        if(!automata.isDeterministic() || !automata.isValid()) {
            throw new IllegalArgumentException();
        }

        FSAAutomata output = deepCopy(automata);
        FSAState sink = new FSAState(false, output.generateUniqueStateName("sink"), 0, 0);
        Set<Character> alphabet = output.getAlphabet();
        for(FSAState s : output.getStates()) {
            Set<Character> curTransactions = s.getTransitions().stream()
                    .map(FSATransition::getSymbol)
                    .collect(Collectors.toSet());
            Set<Character> missingTransitions = new HashSet<>(alphabet);
            missingTransitions.removeAll(curTransactions);

            if (s.isFinal()) {
                for(Character c : missingTransitions) {
                    FSATransition transition = new FSATransition(s, s, c);
                    output.addTransition(transition);
                }
            } else {
                for(Character c : missingTransitions) {
                    if (!output.getStates().contains(sink)) {
                        output.addState(sink);
                    }
                    FSATransition transition = new FSATransition(s, sink, c);
                    output.addTransition(transition);
                }
            }
        }
        return output;
    }

    public static List<List<FSATransition>> testInput(String input, FSAAutomata automata) {
        if(!automata.isValid()) {
            throw new IllegalArgumentException();
        }

        List<List<FSATransition>> validPaths = new LinkedList<>();
        interface Inner {
            void dfs(FSAState currentState, int stringIndex, List<FSATransition> currentPath, Set<FSAState> lambdaVisitedStates);
        }
        Inner r = new Inner() {
            @Override
            public void dfs(FSAState currentState, int stringIndex, List<FSATransition> currentPath, Set<FSAState> lambdaVisitedStates) {
                //base
                if (stringIndex == input.length() && currentState.isFinal()) {
                    validPaths.add(new ArrayList<>(currentPath));
                }

                //lambda transitions
                Set<FSATransition> lambdaTransitions = automata.getTransitions().stream()
                        .filter(t -> t.getSymbol() == null && t.getStart() == currentState)
                        .collect(Collectors.toSet());
                for(FSATransition transition : lambdaTransitions) {
                    if (!lambdaVisitedStates.contains(transition.getEnd())) {
                        Set<FSAState> newLambdaVisited = new HashSet<>(lambdaVisitedStates);
                        newLambdaVisited.add(transition.getEnd());

                        List<FSATransition> newPath = new ArrayList<>(currentPath);
                        newPath.add(transition);

                        dfs(transition.getEnd(), stringIndex, newPath, newLambdaVisited);
                    }
                }

                //symbol transitions
                if(stringIndex < input.length()) {
                    Character currentSymbol = input.charAt(stringIndex);
                    Set<FSATransition> charTransitions = automata.getTransitions().stream()
                            .filter(t -> t.getStart() == currentState && t.getSymbol() == currentSymbol)
                            .collect(Collectors.toSet());
                    for(FSATransition transition : charTransitions) {
                        List<FSATransition> newPath = new ArrayList<>(currentPath);
                        newPath.add(transition);

                        Set<FSAState> newLambdaVisitedStates = new HashSet<>();
                        newLambdaVisitedStates.add(transition.getEnd());

                        dfs(transition.getEnd(), stringIndex + 1, newPath, newLambdaVisitedStates);
                    }
                }
            }
        };
        Set<FSAState> lambdaVisitedStates = new HashSet<>();
        lambdaVisitedStates.add(automata.getInitialState());
        r.dfs(automata.getInitialState(), 0, new LinkedList<>(), lambdaVisitedStates);
        return validPaths;
    }

    private static FSAAutomata deepCopy(FSAAutomata original) {
        if (original == null) return null;

        FSAAutomata copy = new FSAAutomata(original.getName() + "_copy");

        Map<FSAState, FSAState> stateMap = new HashMap<>();

        for(FSAState state : original.getStates()) {
            FSAState clonedState = new FSAState(state.isFinal(), state.getName(), state.x, state.y);
            stateMap.put(state, clonedState);
            copy.addState(clonedState);
            if(original.getInitialState().equals(state)) {
                copy.setInitialState(clonedState);
            }
        }

        for(FSATransition t : original.getTransitions()) {
            FSAState newStart = stateMap.get(t.getStart());
            FSAState newEnd = stateMap.get(t.getEnd());
            FSATransition transition = new FSATransition(newStart, newEnd, t.getSymbol());
            copy.addTransition(transition);
        }

        return copy;
    }
}
