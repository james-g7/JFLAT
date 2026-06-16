package jflat.core.fst.mealy;

import jflat.core.generics.AbstractTransition;

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
}
