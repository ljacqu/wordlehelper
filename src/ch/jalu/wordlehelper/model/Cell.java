package ch.jalu.wordlehelper.model;

public record Cell(char character, Color color) {

    public Cell(char character, Color color) {
        this.character = Character.toUpperCase(character);
        this.color = color;
    }

    public static Cell green(char chr) {
        return new Cell(chr, Color.GREEN);
    }

    public static Cell yellow(char chr) {
        return new Cell(chr, Color.YELLOW);
    }

    public static Cell gray(char chr) {
        return new Cell(chr, Color.GRAY);
    }
}
