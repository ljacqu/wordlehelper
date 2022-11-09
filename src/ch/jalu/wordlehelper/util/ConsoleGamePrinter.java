package ch.jalu.wordlehelper.util;

import ch.jalu.wordlehelper.model.Color;
import ch.jalu.wordlehelper.model.Turn;
import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.HasExactCountPredicate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ConsoleGamePrinter {

    // From https://stackoverflow.com/a/5762502
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_WHITE  = "\u001B[37m";
    public static final String ANSI_BOLD   = "\u001B[1m";
    public static final String ANSI_UNDERLINE = "\u001B[4m";

    private ConsoleGamePrinter() {
    }

    public static void printGameToConsole(List<Turn> turns) {
        for (Turn turn : turns) {
            System.out.println(" " + generateConsoleTextForCells(turn));
        }
    }

    public static String generateConsoleTextForCells(Turn turn) {
        return turn.getCells().stream()
            .map(cell -> getColorCode(cell.color()) + cell.character())
            .collect(Collectors.joining(" ")) + ANSI_RESET;
    }

    public static void printLetterInfoToConsole(Character[] knownChars, Map<Character, CharCountPredicate> predicatesByChar) {
        Set<Character> charsWithKnownPosition = Arrays.stream(knownChars)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder(" ");
        IntStream.rangeClosed('A', 'Z').forEach(charCode -> {
            char chr = (char) charCode;
            CharCountPredicate predicate = predicatesByChar.get(chr);
            if (!isExactlyZeroCountPredicate(predicate)) {
                boolean hasKnownPosition = charsWithKnownPosition.contains(chr);
                sb.append(getColorForChar(hasKnownPosition, predicate))
                    .append(chr)
                    .append(ANSI_RESET)
                    .append(" ");
            }
        });
        System.out.println(sb.toString());
    }

    private static boolean isExactlyZeroCountPredicate(CharCountPredicate predicate) {
        return predicate instanceof HasExactCountPredicate exactCountPredicate
            && exactCountPredicate.getRequiredCount() == 0;
    }

    private static String getColorForChar(boolean hasKnownPosition, CharCountPredicate charCountPredicate) {
        if (charCountPredicate == null) {
            return "";
        }
        if (hasKnownPosition) {
            return charCountPredicate instanceof HasExactCountPredicate
                ? ANSI_UNDERLINE + ANSI_GREEN
                : ANSI_GREEN;
        }
        return charCountPredicate instanceof HasExactCountPredicate
            ? ANSI_UNDERLINE + ANSI_YELLOW
            : ANSI_YELLOW;
    }

    private static String getColorCode(Color color) {
        switch (color) {
            case GREEN:
                return ANSI_BOLD + ANSI_GREEN;
            case YELLOW:
                return ANSI_BOLD + ANSI_YELLOW;
            case GRAY:
                return ANSI_RESET + ANSI_WHITE;
            default:
                throw new IllegalArgumentException("Unhandled color: " + color);
        }
    }
}
