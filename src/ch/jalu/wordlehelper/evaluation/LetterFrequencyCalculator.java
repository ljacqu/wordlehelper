package ch.jalu.wordlehelper.evaluation;

import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.HasExactCountPredicate;
import ch.jalu.wordlehelper.model.predicate.MinimumCountPredicate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;
import static java.util.Collections.emptyMap;

public class LetterFrequencyCalculator {

    public Map<Character, BigDecimal> calculateFrequencyOfLetters(Collection<String> words) {
        return calculateFrequencyOfLetters(words, emptyMap());
    }

    public Map<Character, BigDecimal> calculateFrequencyOfLetters(Collection<String> words,
                                                        Map<Character, CharCountPredicate> knownFrequenciesToSubtract) {
        Map<Character, Integer> countByLetter = new TreeMap<>();
        for (String word : words) {
            for (int i = 0; i < WORD_LENGTH; ++i) {
                countByLetter.merge(word.charAt(i), 1, Integer::sum);
            }
        }
        int total = WORD_LENGTH * words.size();
        for (Map.Entry<Character, Integer> entry : gatherKnownFrequencies(knownFrequenciesToSubtract).entrySet()) {
            Character chr = entry.getKey();
            int subtrahend = entry.getValue() * words.size();
            countByLetter.put(chr, countByLetter.get(chr) - subtrahend);
            total -= subtrahend;
        }

        MathContext mathContext = MathContext.DECIMAL32;
        BigDecimal totalLetters = BigDecimal.valueOf(total);

        Map<Character, BigDecimal> frequencyByLetter = new HashMap<>();
        countByLetter.forEach((letter, count) -> {
            BigDecimal frequency = BigDecimal.valueOf(count).divide(totalLetters, mathContext);
            frequencyByLetter.put(letter, frequency);
        });
        return frequencyByLetter;
    }

    private Map<Character, Integer> gatherKnownFrequencies(Map<Character, CharCountPredicate> predicates) {
        Map<Character, Integer> result = new HashMap<>(predicates.size());
        predicates.forEach((chr, predicate) -> {
            int frequency;
            if (predicate instanceof HasExactCountPredicate h) {
                frequency = h.getRequiredCount();
            } else if (predicate instanceof MinimumCountPredicate m) {
                frequency = m.getMinimumCount();
            } else {
                throw new IllegalStateException();
            }
            if (frequency != 0) {
                result.put(chr, frequency);
            }
        });
        return result;
    }
}
