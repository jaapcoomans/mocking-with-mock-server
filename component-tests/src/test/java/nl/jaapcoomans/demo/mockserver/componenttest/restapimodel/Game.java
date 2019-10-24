package nl.jaapcoomans.demo.mockserver.componenttest.restapimodel;

import java.util.UUID;

public class Game {
    private UUID id;
    private GameStatus status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
