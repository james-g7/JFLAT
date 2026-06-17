package core.fst.mealy;

import core.fsa.FSATransition;
import core.generics.AbstractTransition;

import java.util.Objects;

public class MealyMachineTransition extends AbstractTransition<MealyMachineState, MealyMachineTransition> {
    private char inChar;
    private char outChar;

    public MealyMachineTransition(MealyMachineState start, MealyMachineState end, char inChar, char outChar) {
        super(start, end);
        this.inChar = inChar;
        this.outChar = outChar;
    }

    @Override
    public String getTransitionText() {
        return String.format("%c;%c", inChar, outChar);
    }

    public char getInChar() {
        return inChar;
    }

    public void setInChar(char inChar) {
        this.inChar = inChar;
    }

    public char getOutChar() {
        return outChar;
    }

    public void setOutChar(char outChar) {
        this.outChar = outChar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), inChar, outChar);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MealyMachineTransition other)) return false;
        return Objects.equals(getStart(), other.getStart()) &&
                Objects.equals(getEnd(), other.getEnd()) &&
                Objects.equals(getInChar(), other.getInChar()) &&
                Objects.equals(getOutChar(), other.getOutChar());
    }
}
