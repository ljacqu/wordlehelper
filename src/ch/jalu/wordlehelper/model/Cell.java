package ch.jalu.wordlehelper.model;

public record Cell(Character character, Color color) {

    public static Cell green(Character chr) {
        return new Cell(chr, Color.GREEN);
    }

    public static Cell yellow(Character chr) {
        return new Cell(chr, Color.YELLOW);
    }

    public static Cell gray(Character chr) {
        return new Cell(chr, Color.GRAY);
    }
}
