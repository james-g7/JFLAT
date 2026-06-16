package core.tm;

import core.generics.AbstractTransition;

public class TuringMachineTransition1D extends AbstractTransition<TuringMachineState1D, TuringMachineTransition1D> {
    private Character read;
    private Character write;
    private Direction destination;

    public enum Direction {
        LEFT("L"), RIGHT("R");

        private final String symbol;

        Direction(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }


    public TuringMachineTransition1D(TuringMachineState1D start, TuringMachineState1D end, Character read, Character write, Direction destination) {
        super(start, end);
        this.read = read;
        this.write = write;
        this.destination = destination;
    }

    @Override
    public String getTransitionText() {
        return String.format("%c,%c/%c",
                read == null ? '□': read,
                write == null ? '□': write,
                String.valueOf(destination).charAt(0));
    }

    public Character getRead() {
        return read;
    }

    public void setRead(Character inChar) {
        this.read = inChar;
    }

    public Character getWrite() {
        return write;
    }

    public void setWrite(Character outChar) {
        this.write = outChar;
    }

    public String getDestination() {
        return String.valueOf(destination);
    }

    public void setDestination(char destination) {
        this.destination = Direction.valueOf(String.valueOf(destination));
    }
}
