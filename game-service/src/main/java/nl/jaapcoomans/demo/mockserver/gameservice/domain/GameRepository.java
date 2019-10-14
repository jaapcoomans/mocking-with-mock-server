package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    Optional<Game> findById(UUID id);

    Game persist(Game game);
}
