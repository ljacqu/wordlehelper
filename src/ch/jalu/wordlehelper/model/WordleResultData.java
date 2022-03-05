package ch.jalu.wordlehelper.model;

import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.util.CharCountContainer;
import ch.jalu.wordlehelper.util.HashSetMultimap;

import java.util.Map;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public record WordleResultData(Character[] knownCharactersByIndex,
                               HashSetMultimap<Integer, Character> wrongCharsByIndex,
                               Map<Character, CharCountPredicate> predicatesByChar) {

    public boolean matches(String word) {
        CharCountContainer charCount = new CharCountContainer();
        for (int i = 0; i < WORD_LENGTH; ++i) {
            char chr = word.charAt(i);
            if (!charIsValid(i, chr)) {
                return false;
            }
            charCount.add(chr);
        }

        return predicatesByChar.entrySet().stream()
            .allMatch(entry -> {
                int observedCount = charCount.getCount(entry.getKey());
                return entry.getValue().matches(observedCount);
            });
    }

    private boolean charIsValid(int index, char chr) {
        if (knownCharactersByIndex[index] != null && knownCharactersByIndex[index] != chr) {
            return false;
        }
        return !wrongCharsByIndex.contains(index, chr);
    }
}
