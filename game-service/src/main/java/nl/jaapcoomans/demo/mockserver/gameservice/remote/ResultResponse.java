package nl.jaapcoomans.demo.mockserver.gameservice.remote;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.Result;

public class ResultResponse {
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

    public Result toResult() {
        return new Result(blackPins, whitePins);
    }
}
