package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import nl.jaapcoomans.demo.mockserver.gameservice.remote.TournamentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class GameServiceTest {
    private static final Random RND = new SecureRandom();

    private CodeGenerator codeGenerator = mock(CodeGenerator.class);
    private CodeChecker codeChecker = mock(CodeChecker.class);
    private TournamentService tournamentService = mock(TournamentService.class);

    private GameService gameService = new GameService(codeGenerator, codeChecker, tournamentService);;

    @BeforeEach
    void resetMocks() {
        reset(codeGenerator, codeChecker, tournamentService);
    }

    @Test
    @DisplayName("When a game is started, it must be in progress.")
    void testStartGame() {
        // Given
        when(codeGenerator.generateCode()).thenReturn(createACode());

        // When a new game is started
        var game = gameService.startNewGame();

        // Then the game must be in progress and only the generator be called.
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);

        verify(codeGenerator).generateCode();
        verifyZeroInteractions(codeChecker);
        verifyZeroInteractions(tournamentService);
    }

    void testGuessCode() {
        // Given
        var theSolution = createACode();
        when(codeGenerator.generateCode()).thenReturn(createACode());
        var game = gameService.startNewGame();

        // When
        this.gameService.guessCode(game.getId(), createADifferentCode(theSolution));
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
