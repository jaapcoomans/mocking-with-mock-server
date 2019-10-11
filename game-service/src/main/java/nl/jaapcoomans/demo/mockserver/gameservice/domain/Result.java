package nl.jaapcoomans.demo.mockserver.gameservice.domain;

public class Result {
    private final int blackPins;
    private final int whitePins;

    public Result(int blackPins, int whitePins) {
        this.blackPins = blackPins;
        this.whitePins = whitePins;
    }

    public int getBlackPins() {
        return blackPins;
    }

    public int getWhitePins() {
        return whitePins;
    }
}
