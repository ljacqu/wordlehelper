package ch.jalu.wordlehelper.evaluation;

import ch.jalu.wordlehelper.model.Cell;
import ch.jalu.wordlehelper.model.Color;
import ch.jalu.wordlehelper.model.Turn;
import ch.jalu.wordlehelper.model.WordleResultData;
import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.MinimumCountPredicate;
import ch.jalu.wordlehelper.util.HashSetMultimap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public class GameDataCreator {

    public WordleResultData constructResultData(List<Turn> turns) {
        Character[] knownChars = new Character[WORD_LENGTH];
        HashSetMultimap<Integer, Character> wrongCharsByPosition = new HashSetMultimap<>();
        Map<Character, CharCountPredicate> predicatesByChar = new HashMap<>();

        for (Turn turn : turns) {
            CharCountPredicateBuilder charCountPredicateBuilder = new CharCountPredicateBuilder();
            List<Cell> cells = turn.getCells();
            for (int i = 0; i < cells.size(); i++) {
                Cell cell = cells.get(i);
                Color color = cell.color();
                char character = cell.character();

                charCountPredicateBuilder.register(character, color);
                if (color == Color.GREEN) {
                    knownChars[i] = character;
                } else  {
                    wrongCharsByPosition.put(i, character);
                }
            }

            charCountPredicateBuilder.predicatesByChar.forEach((chr, predicate) -> {
                predicatesByChar.merge(chr, predicate, CharCountPredicate::merge);
            });
        }
        return new WordleResultData(knownChars, wrongCharsByPosition, predicatesByChar);
    }

    public static final class CharCountPredicateBuilder {

        // todo check if better to initialize map without size?
        private final Map<Character, CharCountPredicate> predicatesByChar = new HashMap<>(WORD_LENGTH);

        public void register(Character chr, Color color) {
            CharCountPredicate pred = predicatesByChar.get(chr);
            if (pred == null) {
                pred = MinimumCountPredicate.of(0);
            }
            predicatesByChar.put(chr, pred.update(color));
        }

        public Map<Character, CharCountPredicate> getPredicatesByChar() {
            return predicatesByChar;
        }
    }
}
