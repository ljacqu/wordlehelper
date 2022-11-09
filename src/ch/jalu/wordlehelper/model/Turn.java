package ch.jalu.wordlehelper.model;

import java.util.ArrayList;
import java.util.List;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public class Turn {

    private final List<Cell> cells;

    public Turn(Cell... cells) {
        this(List.of(cells));
    }

    public Turn(List<Cell> cells) {
        if (cells.size() != WORD_LENGTH) {
            throw new IllegalArgumentException("Expected " + WORD_LENGTH + " cells but found " + cells.size());
        }
        this.cells = List.copyOf(cells);
    }

    /**
     * To display the following Wordle state:
     * <p>
     *  <b><font color="yellow">T</font><font color="gray">ALES</font></b>
     *  <br><b><font color="gray">C</font><font color="yellow">OU</font><font color="gray">R</font><font color="green">T</font></b>
     * <p>
     * use the following code:
     * <pre>{@code List<Turn> turns = List.of(
     *   Turn.of("T?ALES"),
     *   Turn.of("CO?U?RT!")
     * );}</pre>
     */
    public static Turn of(String cellRepresentation) {
        List<Cell> cells = new ArrayList<>(WORD_LENGTH);
        Character currentLetter = null;
        for (int i = 0; i < cellRepresentation.length(); ++i) {
            char c = cellRepresentation.charAt(i);
            if (Character.isAlphabetic(c)) {
                if (currentLetter != null) {
                    cells.add(Cell.gray(currentLetter));
                }
                currentLetter = Character.toUpperCase(c);
            } else if (c == '?') {
                cells.add(Cell.yellow(currentLetter));
                currentLetter = null;
            } else if (c == '!') {
                cells.add(Cell.green(currentLetter));
                currentLetter = null;
            } else if (!Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Unexpected character: " + c);
            }
        }
        if (currentLetter != null) {
            cells.add(Cell.gray(currentLetter));
        }
        validateCells(cells);
        return new Turn(cells);
    }

    public List<Cell> getCells() {
        return cells;
    }

    private static void validateCells(List<Cell> cells) {
        cells.forEach(cell -> {
            if (cell.character() == null) {
                throw new IllegalArgumentException("Error in letter representation. Please try again.");
            }
        });
    }
}
