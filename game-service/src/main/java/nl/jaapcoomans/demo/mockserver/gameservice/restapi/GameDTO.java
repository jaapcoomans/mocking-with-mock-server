package nl.jaapcoomans.demo.mockserver.gameservice.restapi;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.Game;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameStatus;

import java.util.UUID;

public class GameDTO {
    private UUID id;
    private GameStatus status;

    static GameDTO fromGame(Game game) {
        return new GameDTO(game.getId(), game.getStatus());
    }

    private GameDTO(UUID id, GameStatus status) {
        this.id = id;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public GameStatus getStatus() {
        return status;
    }
}
