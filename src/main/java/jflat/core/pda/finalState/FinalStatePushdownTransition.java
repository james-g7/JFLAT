package core.pda.finalState;

import core.generics.AbstractTransition;

public class FinalStatePushdownTransition extends AbstractTransition<FinalStatePushdownState, FinalStatePushdownTransition> {

    private Character inSymbol;
    private String popSymbol;
    private String pushSymbols;

    public FinalStatePushdownTransition(FinalStatePushdownState start, FinalStatePushdownState end, Character inSymbol, String popSymbol, String pushSymbols) {
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
