package ch.jalu.wordlehelper.model.predicate;

import ch.jalu.wordlehelper.model.Color;

public sealed interface CharCountPredicate
    permits HasExactCountPredicate, MinimumCountPredicate {

    boolean matches(int count);

    CharCountPredicate merge(CharCountPredicate other);

    CharCountPredicate update(Color color);

}
