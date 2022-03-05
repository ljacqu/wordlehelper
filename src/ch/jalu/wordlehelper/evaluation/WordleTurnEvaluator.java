package ch.jalu.wordlehelper.evaluation;

import ch.jalu.wordlehelper.model.Cell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public class WordleTurnEvaluator {

    private final BigDecimal scoreGreen  = BigDecimal.ONE;
    private final BigDecimal scoreYellow = new BigDecimal("0.5");
    private final BigDecimal scoreGray   = BigDecimal.ZERO;

    public BigDecimal calculateScore(String playedWord, String correctWord) {
        int[] stats = evaluate0(playedWord, correctWord);
        return (BigDecimal.valueOf(stats[0]).multiply(scoreGreen))
           .add(BigDecimal.valueOf(stats[1]).multiply(scoreYellow))
           .add(BigDecimal.valueOf(stats[2]).multiply(scoreGray));
    }

    public List<Cell> evaluateCells(String playedWord, String result) {
        Cell[] cells = new Cell[WORD_LENGTH];
        List<Character> resultNonMatchedChars = new ArrayList<>(WORD_LENGTH);

        for (int i = 0; i < WORD_LENGTH; ++i) {
            char playedChar = playedWord.charAt(i);
            char resultChar = result.charAt(i);
            if (playedChar == resultChar) {
                cells[i] = Cell.green(playedChar);
            } else {
                resultNonMatchedChars.add(resultChar);
            }
        }

        for (int i = 0; i < WORD_LENGTH; ++i) {
            if (cells[i] == null) {
                Character playedChar = playedWord.charAt(i);
                boolean isYellow = resultNonMatchedChars.remove(playedChar);
                cells[i] = isYellow ? Cell.yellow(playedChar) : Cell.gray(playedChar);
            }
        }
        return Arrays.asList(cells);
    }

    private static int[] evaluate0(String playedWord, String correctWord) {
        int greens = 0;
        List<Character> wrongPlayedChars = new ArrayList<>(WORD_LENGTH);
        List<Character> notFoundActualChars = new ArrayList<>(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; ++i) {
            if (playedWord.charAt(i) == correctWord.charAt(i)) {
                ++greens;
            } else {
                wrongPlayedChars.add(playedWord.charAt(i));
                notFoundActualChars.add(correctWord.charAt(i));
            }
        }

        int yellows = 0;
        for (Character wrongPlayedChar : wrongPlayedChars) {
            if (notFoundActualChars.remove(wrongPlayedChar)) {
                ++yellows;
            }
        }

        int grays = WORD_LENGTH - greens - yellows;
        return new int[] {
            greens,
            yellows,
            grays
        };
    }
}
