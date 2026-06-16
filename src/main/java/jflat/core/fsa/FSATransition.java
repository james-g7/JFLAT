package jflat.core.fsa;

import jflat.core.generics.AbstractTransition;

import java.util.Objects;

public class FSATransition extends AbstractTransition<FSAState, FSATransition> {
    private Character symbol;

    public FSATransition(FSAState start, FSAState end, Character symbol) {
        super(start, end);
        this.symbol = symbol;
    }

    public Character getSymbol() {
        return symbol;
    }

    public void setSymbol(Character symbol) {
        this.symbol = symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), symbol);
    }


    @Override
    public String getTransitionText() {
        if (symbol == null) {
            return "λ";
        }
        return String.valueOf(symbol);
    }
}