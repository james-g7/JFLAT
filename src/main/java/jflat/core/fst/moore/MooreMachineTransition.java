package core.fst.moore;

import core.fst.mealy.MealyMachineTransition;
import core.generics.AbstractTransition;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MooreMachineTransition other)) return false;
        return Objects.equals(getStart(), other.getStart()) &&
                Objects.equals(getEnd(), other.getEnd()) &&
                Objects.equals(getSymbol(), other.getSymbol());
    }
}
