package nl.jaapcoomans.demo.mockserver.gameservice.remote;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameStatus;

import java.util.UUID;

public class GameEndedBody {
    private UUID gameId;
    private GameStatus status;
    private int guesses;

    GameEndedBody(UUID gameId, GameStatus status, int guesses) {
        this.gameId = gameId;
        this.status = status;
        this.guesses = guesses;
    }

    public UUID getGameId() {
        return gameId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getGuesses() {
        return guesses;
    }
}
