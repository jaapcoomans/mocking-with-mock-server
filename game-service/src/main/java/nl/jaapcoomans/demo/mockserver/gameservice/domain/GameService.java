package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GameService {
    private GameRepository gameRepository;

    private CodeGenerator codeGenerator;
    private CodeChecker codeChecker;
    private TournamentService tournamentService;

    public GameService(GameRepository gameRepository, CodeGenerator codeGenerator, CodeChecker codeChecker, TournamentService tournamentService) {
        this.gameRepository = gameRepository;
        this.codeGenerator = codeGenerator;
        this.codeChecker = codeChecker;
        this.tournamentService = tournamentService;
    }

    public Game startNewGame() {
        var code = this.codeGenerator.generateCode();
        var game = new Game(code);
        this.gameRepository.persist(game);

        return game;
    }

    public Result guessCode(UUID gameId, Code guess) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game does not exist"));

        var result = game.guess(guess, this.codeChecker);
        if(game.isFinished()) {
            this.tournamentService.gameEnded(game.getId(), game.getStatus(), game.getNumberOfGuesses());
        }

        return result;
    }

    public Code getSolution(UUID gameId) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game does not exist"));

        return game.getCode();
    }

    public List<Game> findAll() {
        return this.gameRepository.findAll();
    }

    public Optional<Game> findById(UUID id) {
        return gameRepository.findById(id);
    }
}
