package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import java.util.UUID;

public interface TournamentService {
    void gameEnded(UUID gameId, GameStatus result, int guesses);
}
