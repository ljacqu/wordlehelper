package ch.jalu.wordlehelper.model.predicate;

import ch.jalu.wordlehelper.model.Color;

import java.util.function.IntPredicate;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public final class MinimumCountPredicate implements CharCountPredicate {

    private static final MinimumCountPredicate[] CACHE = createCache();

    private final int minimumCount;
    private final IntPredicate intPredicate;

    private MinimumCountPredicate(int minimumCount) {
        this.minimumCount = minimumCount;
        this.intPredicate = i -> i >= minimumCount;
    }

    public static MinimumCountPredicate of(int minimumCount) {
        return CACHE[minimumCount];
    }

    public int getMinimumCount() {
        return minimumCount;
    }

    public IntPredicate getIntPredicate() {
        return intPredicate;
    }

    @Override
    public boolean matches(int count) {
        return count >= minimumCount;
    }

    @Override
    public CharCountPredicate merge(CharCountPredicate other) {
        if (this == other) {
            return this;
        }
        if (other instanceof HasExactCountPredicate o) {
            return o;
            // todo use o.merge(this);
        } else if (other instanceof MinimumCountPredicate o) {
            return this.minimumCount >= o.minimumCount ? this : o;
        } else {
            throw new IllegalStateException("Unexpected predicate to merge with of class " + other.getClass());
        }
    }

    @Override
    public CharCountPredicate update(Color color) {
        if (color == Color.GRAY) {
            return HasExactCountPredicate.of(this.minimumCount);
        }
        return CACHE[this.minimumCount + 1];
    }

    @Override
    public String toString() {
        return "CharCountPredicate(count >= " + minimumCount + ")";
    }

    private static MinimumCountPredicate[] createCache() {
        MinimumCountPredicate[] cache = new MinimumCountPredicate[WORD_LENGTH + 1];
        for (int i = 0; i <= WORD_LENGTH; ++i) {
            cache[i] = new MinimumCountPredicate(i);
        }
        return cache;
    }
}