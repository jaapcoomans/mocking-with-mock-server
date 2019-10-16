package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.SecureRandom;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GameTest {
    private static final Random RND = new SecureRandom();

    @Test
    @DisplayName("When a new game is created, it gets an ID, has status in progress and is not finished.")
    void testNewGame() {
        // When
        var game = new Game(createACode());

        // Then
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getId()).isNotNull();
        assertThat(game.isFinished()).isFalse();
    }

    @Test
    @DisplayName("A game that is still in progress will not reveal the code.")
    void testGameNotFinishedGetCode() {
        // Given
        var game = new Game(createACode());

        // When
        var exception = catchThrowable(game::getCode);

        // Then
        assertThat(exception)
                .isInstanceOf(IllegalGameSateException.class)
                .hasMessageContaining("The game is still in progress");
    }

    @Test
    @DisplayName("Guessing the right code finishes the game as a win.")
    void testWinningAGame() {
        // Given
        var theCode = createACode();
        var game = new Game(theCode);

        var mockChecker = Mockito.mock(CodeChecker.class);
        when(mockChecker.checkCode(theCode, theCode)).thenReturn(new Result(4, 0));

        // When the right code is guessed
        game.guess(theCode, mockChecker);

        // Then the game is finished as a win
        assertThat(game.isFinished()).isTrue();
        assertThat(game.getStatus()).isEqualTo(GameStatus.WON);
        assertThat(game.getCode()).isEqualTo(theCode);
    }

    @Test
    @DisplayName("Guessing the wrong code 10 times finishes the game as a loss.")
    void testLosingAGame() {
        // Given
        var theCode = createACode();
        var game = new Game(theCode);

        var mockChecker = Mockito.mock(CodeChecker.class);
        when(mockChecker.checkCode(eq(theCode), any())).thenReturn(new Result(0, 0));

        // When making a wrong guess 10 times
        for (int i = 0; i < 10; i++) {
            game.guess(createADifferentCode(theCode), mockChecker);
        }

        // Then the game is lost and finished
        assertThat(game.isFinished()).isTrue();
        assertThat(game.getStatus()).isEqualTo(GameStatus.LOST);
    }

    @Test
    @DisplayName("A game that was already won accepts no more guesses")
    void testWonGameIsNotPlayable() {
        // Given a won game
        var theCode = createACode();
        var game = new Game(theCode);

        var mockChecker = Mockito.mock(CodeChecker.class);
        when(mockChecker.checkCode(theCode, theCode)).thenReturn(new Result(4, 0));

        game.guess(theCode, mockChecker);

        // When one more guess is made
        var exception = catchThrowable(() -> game.guess(createACode(), mockChecker));

        // Then an exception is thrown
        assertThat(exception)
                .isInstanceOf(IllegalGameSateException.class)
                .hasMessageContaining("No more guessing, the game is already finished!");
    }

    @Test
    @DisplayName("A game that was already lost accepts no more guesses")
    void testLostGameIsNotPlayable() {
        // Given a lost game
        var theCode = createACode();
        var game = new Game(theCode);

        var mockChecker = Mockito.mock(CodeChecker.class);
        when(mockChecker.checkCode(eq(theCode), any())).thenReturn(new Result(0, 0));

        for (int i = 0; i < 10; i++) {
            game.guess(createADifferentCode(theCode), mockChecker);
        }

        // When one more guess is made
        var exception = catchThrowable(() -> game.guess(createACode(), mockChecker));

        // Then an exception is thrown
        assertThat(exception)
                .isInstanceOf(IllegalGameSateException.class)
                .hasMessageContaining("No more guessing, the game is already finished!");
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
