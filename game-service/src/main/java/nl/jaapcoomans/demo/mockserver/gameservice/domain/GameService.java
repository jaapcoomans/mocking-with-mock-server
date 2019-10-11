package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import nl.jaapcoomans.demo.mockserver.gameservice.remote.TournamentService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameService {
    private final Map<UUID, Game> games = new HashMap<>();

    private CodeGenerator codeGenerator;
    private CodeChecker codeChecker;
    private TournamentService tournamentService;

    public GameService(CodeGenerator codeGenerator, CodeChecker codeChecker, TournamentService tournamentService) {
        this.codeGenerator = codeGenerator;
        this.codeChecker = codeChecker;
        this.tournamentService = tournamentService;
    }

    public Game startNewGame() {
        var code = this.codeGenerator.generateCode();
        var game = new Game(this.codeChecker, code);
        this.games.put(game.getId(), game);

        return game;
    }

    public Result guessCode(UUID gameId, Code guess) {
        Game game = this.games.get(gameId);
        if (game == null) {
            throw new RuntimeException("Game does not exist");
        }

        return game.guess(guess);
    }

    public Code getSolution(UUID gameId) {
        Game game = this.games.get(gameId);
        if (game == null) {
            throw new RuntimeException("Game does not exist");
        }

        return game.getCode();
    }
}
