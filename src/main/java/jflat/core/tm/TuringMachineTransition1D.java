package core.tm;

import core.generics.AbstractTransition;

import java.util.Objects;

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

    public Direction getDestination() {
        return destination;
    }

    public void setDestination(Direction destination) {
        this.destination = destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), read, write, destination);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TuringMachineTransition1D other)) return false;
        return Objects.equals(getStart(), other.getStart()) &&
                Objects.equals(getEnd(), other.getEnd()) &&
                Objects.equals(getRead(), other.getRead()) &&
                Objects.equals(getWrite(), other.getWrite()) &&
                Objects.equals(getDestination(), other.getDestination());
    }
}
