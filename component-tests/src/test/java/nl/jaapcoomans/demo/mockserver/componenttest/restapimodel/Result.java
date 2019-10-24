package nl.jaapcoomans.demo.mockserver.componenttest.restapimodel;

public class Result {
    private int blackPins;
    private int whitePins;

    public void setBlackPins(int blackPins) {
        this.blackPins = blackPins;
    }

    public void setWhitePins(int whitePins) {
        this.whitePins = whitePins;
    }

    public int getBlackPins() {
        return blackPins;
    }

    public int getWhitePins() {
        return whitePins;
    }
}