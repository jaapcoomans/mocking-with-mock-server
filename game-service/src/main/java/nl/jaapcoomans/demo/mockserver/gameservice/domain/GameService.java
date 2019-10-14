package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import nl.jaapcoomans.demo.mockserver.gameservice.remote.TournamentService;

import java.util.UUID;

public class GameService {
    private GameRepository gameRepository;

    private CodeGenerator codeGenerator;
    private CodeChecker codeChecker;
    private TournamentService tournamentService;

    GameService(GameRepository gameRepository, CodeGenerator codeGenerator, CodeChecker codeChecker, TournamentService tournamentService) {
        this.gameRepository = gameRepository;
        this.codeGenerator = codeGenerator;
        this.codeChecker = codeChecker;
        this.tournamentService = tournamentService;
    }

    Game startNewGame() {
        var code = this.codeGenerator.generateCode();
        var game = new Game(code);
        this.gameRepository.persist(game);

        return game;
    }

    Result guessCode(UUID gameId, Code guess) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game does not exist"));

        return game.guess(guess, this.codeChecker);
    }

    public Code getSolution(UUID gameId) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game does not exist"));

        return game.getCode();
    }
}
