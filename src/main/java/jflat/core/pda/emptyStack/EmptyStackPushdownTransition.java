package core.pda.emptyStack;

import core.generics.AbstractTransition;

public class EmptyStackPushdownTransition extends AbstractTransition<EmptyStackPushdownState, EmptyStackPushdownTransition> {

    private Character inSymbol;
    private String popSymbol;
    private String pushSymbols;

    public EmptyStackPushdownTransition(EmptyStackPushdownState start, EmptyStackPushdownState end, Character inSymbol, String popSymbol, String pushSymbols) {
        super(start, end);
        this.inSymbol = inSymbol;
        this.popSymbol = popSymbol;
        this.pushSymbols = pushSymbols;
    }

    @Override
    public String getTransitionText() {
        String formattedPushSymbols = (pushSymbols == null) ? "λ" : pushSymbols;
        formattedPushSymbols = formattedPushSymbols.equals("empty") ? "Z₀" : formattedPushSymbols;

        String formattedPopSymbol = (popSymbol == null) ? "λ" : popSymbol;
        formattedPopSymbol = formattedPopSymbol.equals("empty") ? "Z₀" : formattedPopSymbol;

        return String.format("%s,%s/%s",
                inSymbol == null ? "λ" : inSymbol,
                formattedPopSymbol,
                formattedPushSymbols);
    }

    public Character getInSymbol() {
        return inSymbol;
    }

    public void setInSymbol(Character inSymbol) {
        this.inSymbol = inSymbol;
    }

    public String getPopSymbol() {
        return popSymbol;
    }

    public void setPopSymbol(String popSymbol) {
        this.popSymbol = popSymbol;
    }

    public String getPushSymbols() {
        return pushSymbols;
    }

    public void setPushSymbols(String pushSymbols) {
        this.pushSymbols = pushSymbols;
    }
}
