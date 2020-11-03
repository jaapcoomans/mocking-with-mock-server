package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GameServiceTest {
    private static final Random RND = new SecureRandom();

    private static final Result ALL_CORRECT = new Result(4, 0);

    private final GameRepository repository = mock(GameRepository.class);
    private final CodeGenerator codeGenerator = mock(CodeGenerator.class);
    private final CodeChecker codeChecker = mock(CodeChecker.class);
    private final TournamentService tournamentService = mock(TournamentService.class);

    private final GameService gameService =
            new GameService(repository, codeGenerator, codeChecker, tournamentService);

    @BeforeEach
    void resetMocks() {
        reset(repository, codeGenerator, codeChecker, tournamentService);
    }

    @Test
    @DisplayName("When a game is started, it must be persisted and in progress.")
    void testStartGame() {
        // Given
        when(codeGenerator.generateCode()).thenReturn(createACode());

        // When a new game is started
        var game = gameService.startNewGame();

        // Then the game must be in progress and only the generator be called.
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);

        verify(repository).persist(game);
        verify(codeGenerator).generateCode();
        verifyNoInteractions(codeChecker);
        verifyNoInteractions(tournamentService);
    }

    @Test
    @DisplayName("When a guess is made for a game that does not exist, an exception is thrown.")
    void testGuessNonExistingGame() {
        // Given
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        var exception = catchThrowable(() -> gameService.guessCode(UUID.randomUUID(), createACode()));

        // Then
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Game does not exist");

        verifyNoInteractions(codeGenerator);
        verifyNoInteractions(codeChecker);
        verifyNoInteractions(tournamentService);
    }

    @Test
    @DisplayName("When the solution is requested for a game that does not exist, an exception is thrown.")
    void testGetSolutionNonExistingGame() {
        // Given
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        var exception = catchThrowable(() -> gameService.getSolution(UUID.randomUUID()));

        // Then
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Game does not exist");

        verifyNoInteractions(codeGenerator);
        verifyNoInteractions(codeChecker);
        verifyNoInteractions(tournamentService);
    }

    @Test
    @DisplayName("When a guess is made, it is delegated to the corresponding Game.")
    void testGuessDelegatesToGame() {
        // Given
        var game = mock(Game.class);
        when(repository.findById(any())).thenReturn(Optional.of(game));
        var guess = createACode();

        // When
        this.gameService.guessCode(game.getId(), guess);

        // Then
        verify(game).guess(eq(guess), any());
        verifyNoInteractions(codeGenerator);
        verifyNoInteractions(tournamentService);
    }

    @Test
    @DisplayName("When a solution is requested, it is delegated to the corresponding Game.")
    void testGetSolutionDelegatesToGame() {
        // Given
        var game = mock(Game.class);
        when(repository.findById(any())).thenReturn(Optional.of(game));

        // When
        this.gameService.getSolution(UUID.randomUUID());

        // Then
        verify(game).getCode();
        verifyNoInteractions(codeGenerator);
        verifyNoInteractions(codeChecker);
        verifyNoInteractions(tournamentService);
    }

    @Test
    @DisplayName("When a game is won, the tournament-service is called")
    void testGameWonCallsTournamentService() {
        // Given
        var code = createACode();
        var game = new Game(code);
        when(repository.findById(game.getId())).thenReturn(Optional.of(game));

        when(codeChecker.checkCode(any(), any())).thenReturn(ALL_CORRECT);

        // When
        this.gameService.guessCode(game.getId(), code);

        // Then
        verify(this.tournamentService).gameEnded(game.getId(), GameStatus.WON, 1);
    }

    @Test
    @DisplayName("When a game is lost, the tournament-service is called")
    void testGameLostCallsTournamentService() {
        // Given
        var code = createACode();
        var game = new Game(code);
        when(repository.findById(game.getId())).thenReturn(Optional.of(game));

        when(codeChecker.checkCode(any(), any())).thenReturn(createANonWinningResult());

        // When
        var wrongGuess = createADifferentCode(code);
        for (int i = 0; i < 10; i++) {
            this.gameService.guessCode(game.getId(), wrongGuess);
        }

        // Then
        verify(this.tournamentService).gameEnded(game.getId(), GameStatus.LOST, 10);
    }

    private static Code createACode() {
        return new Code(pickAColor(), pickAColor(), pickAColor(), pickAColor());
    }

    private static Code createADifferentCode(Code notThisCode) {
        Code differentCode = createACode();
        while (differentCode.equals(notThisCode)) {
            differentCode = createACode();
        }
        return differentCode;
    }

    private static Result createANonWinningResult() {
        final var blackPins = RND.nextInt(4);
        final var whitePins = RND.nextInt(5 - blackPins);
        return new Result(blackPins, whitePins);
    }

    private static ColoredPin pickAColor() {
        return ColoredPin.values()[RND.nextInt(ColoredPin.values().length)];
    }
}
