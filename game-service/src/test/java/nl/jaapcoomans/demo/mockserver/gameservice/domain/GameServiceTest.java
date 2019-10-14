package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import nl.jaapcoomans.demo.mockserver.gameservice.remote.TournamentService;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class GameServiceTest {
    private static final Random RND = new SecureRandom();

    private GameRepository repository = mock(GameRepository.class);
    private CodeGenerator codeGenerator = mock(CodeGenerator.class);
    private CodeChecker codeChecker = mock(CodeChecker.class);
    private TournamentService tournamentService = mock(TournamentService.class);

    private GameService gameService = new GameService(repository, codeGenerator, codeChecker, tournamentService);

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
        verifyZeroInteractions(codeChecker);
        verifyZeroInteractions(tournamentService);
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

        verifyZeroInteractions(codeGenerator);
        verifyZeroInteractions(codeChecker);
        verifyZeroInteractions(tournamentService);
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

        verifyZeroInteractions(codeGenerator);
        verifyZeroInteractions(codeChecker);
        verifyZeroInteractions(tournamentService);
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
        verifyZeroInteractions(codeGenerator);
        verifyZeroInteractions(tournamentService);
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
        verifyZeroInteractions(codeGenerator);
        verifyZeroInteractions(codeChecker);
        verifyZeroInteractions(tournamentService);
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

    private static ColoredPin pickAColor() {
        return ColoredPin.values()[RND.nextInt(ColoredPin.values().length)];
    }
}
