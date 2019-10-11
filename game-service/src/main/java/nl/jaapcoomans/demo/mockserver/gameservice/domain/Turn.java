package nl.jaapcoomans.demo.mockserver.gameservice.domain;

class Turn {
    private final Code guess;
    private final Result result;

    Turn(Code guess, Result result) {
        this.guess = guess;
        this.result = result;
    }

    boolean isWinningTurn() {
        return this.guess.numberOfPins() == this.result.getBlackPins();
    }
}
