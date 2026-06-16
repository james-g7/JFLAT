package jflat.core.fst.moore;

import jflat.core.generics.AbstractTransition;

public class MooreMachineTransition extends AbstractTransition<MooreMachineState, MooreMachineTransition> {
    private char symbol;

    public MooreMachineTransition(MooreMachineState start, MooreMachineState end, char symbol) {
        super(start, end);
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getTransitionText() {
        return String.valueOf(symbol);
    }
}
