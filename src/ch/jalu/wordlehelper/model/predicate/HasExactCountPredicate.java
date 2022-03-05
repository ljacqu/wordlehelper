package ch.jalu.wordlehelper.model.predicate;

import ch.jalu.wordlehelper.model.Color;

import java.util.function.IntPredicate;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public final class HasExactCountPredicate implements CharCountPredicate {

    private static final HasExactCountPredicate[] CACHE = createCache();

    private final int requiredCount;
    private final IntPredicate intPredicate;

    private HasExactCountPredicate(int requiredCount) {
        this.requiredCount = requiredCount;
        this.intPredicate = i -> i == requiredCount;
    }

    public static HasExactCountPredicate of(int requiredCount) {
        return CACHE[requiredCount];
    }

    public IntPredicate getIntPredicate() {
        return intPredicate;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    @Override
    public boolean matches(int count) {
        return count == requiredCount;
    }

    @Override
    public CharCountPredicate merge(CharCountPredicate other) {
        return this;
    }

    @Override
    public CharCountPredicate update(Color color) {
        if (color == Color.GRAY) {
            return this;
        }
        return CACHE[this.requiredCount + 1];
    }

    @Override
    public String toString() {
        return "CharCountPredicate(count = " + requiredCount + ")";
    }

    private static HasExactCountPredicate[] createCache() {
        HasExactCountPredicate[] cache = new HasExactCountPredicate[WORD_LENGTH + 1];
        for (int i = 0; i <= WORD_LENGTH; ++i) {
            cache[i] = new HasExactCountPredicate(i);
        }
        return cache;
    }
}