package server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

class GameLogicTest {
    private final GameLogic gameLogic = new GameLogic();

    // ValidateGuess tests-suite - 1. trivial cases. 2. Out of range numbers. 3. Spaces/Empty strings. 4. parseInt
    // invalid input
    @Test
    void testValidateGuess() {
        assertEquals(10, gameLogic.validateGuess("10"));
        assertEquals(1, gameLogic.validateGuess("1"));
        assertEquals(100, gameLogic.validateGuess("100"));
    }

    @Test
    void testValidateGuess_InvalidNumbers() {
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("0"));
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("101"));
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("-1"));
    }

    @Test
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void testValidateGuess_EmptyOrNull(String input) {
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess(input));
    }
    @Test
    void testValidateGuess_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("abc"));
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("10.5"));
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("10a"));
        assertThrows(IllegalArgumentException.class, () ->
                gameLogic.validateGuess("1 4"));
    }

    private GameLogic createGameLogicWithFixedRandom(int fixedNumber) {
        return new GameLogic(new Random() {
            @Override
            public int nextInt(int origin, int bound) {
                return fixedNumber;
            }

            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });
    }

    @Test
    void testCheckGuessCorrectness{
        // Even number test, under 50
        GameLogic fixedRandomGameLogic = createGameLogicWithFixedRandom(42);
        assertTrue(gameLogic.checkGuessCorrectness(48));
        assertFalse(gameLogic.checkGuessCorrectness(42));

        // Even number, above 50
        GameLogic gameLogic = createGameLogicWithFixedRandom(26);
        assertTrue(gameLogic.checkGuessCorrectness(31));
        assertFalse(gameLogic.checkGuessCorrectness(62));

        // Odd number test, under 50
        GameLogic gameLogic = createGameLogicWithFixedRandom(41);
        assertTrue(gameLogic.checkGuessCorrectness(86));
        assertFalse(gameLogic.checkGuessCorrectness(41));

        // Odd number test, above 50
        GameLogic gameLogic = createGameLogicWithFixedRandom(47);
        assertTrue(gameLogic.checkGuessCorrectness(37));
        assertFalse(gameLogic.checkGuessCorrectness(47));

        // Large number
        GameLogic gameLogic = createGameLogicWithFixedRandom(100);
        assertTrue(gameLogic.checkGuessCorrectness(2));
        assertFalse(gameLogic.checkGuessCorrectness(100));


    }
    }
}
