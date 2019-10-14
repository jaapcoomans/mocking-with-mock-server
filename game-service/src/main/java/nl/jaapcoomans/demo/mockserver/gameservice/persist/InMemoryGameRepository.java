package nl.jaapcoomans.demo.mockserver.gameservice.persist;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.Game;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryGameRepository implements GameRepository {
    private final Map<UUID, Game> games = new HashMap<>();

    @Override
    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(this.games.get(id));
    }

    @Override
    public Game persist(Game game) {
        this.games.put(game.getId(), game);
        return game;
    }
}
