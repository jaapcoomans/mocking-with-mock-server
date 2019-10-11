package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Game {
    private static final int MAX_TURNS = 10;

    private CodeChecker codeChecker;

    private UUID id = UUID.randomUUID();
    private final List<Turn> turns = new ArrayList<>();

    private final Code code;

    private GameStatus status = GameStatus.IN_PROGRESS;

    Game(CodeChecker codeChecker, Code code) {
        this.codeChecker = codeChecker;
        this.code = code;
    }

    Code getCode() {
        if (this.status == GameStatus.IN_PROGRESS) {
            throw new IllegalGameSateException("I'm not telling the code! The game is still in progress!");
        }
        return this.code;
    }

    boolean isFinished() {
        return this.turns.size() >= MAX_TURNS || this.turns.stream().anyMatch(Turn::isWinningTurn);
    }

    UUID getId() {
        return id;
    }

    GameStatus getStatus() {
        return status;
    }

    Result guess(Code guess) {
        if (this.isFinished()) {
            throw new IllegalGameSateException("No more guessing, the game is already finished!");
        }

        var result = this.codeChecker.checkCode(code, guess);
        if (result.getBlackPins() == this.code.numberOfPins()) {
            this.status = GameStatus.WON;
        }

        this.turns.add(new Turn(guess, result));
        if (this.turns.size() >= MAX_TURNS && this.status != GameStatus.WON) {
            this.status = GameStatus.LOST;
        }

        return result;
    }
}
