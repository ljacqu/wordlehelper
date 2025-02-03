package ch.jalu.wordlehelper.evaluation;

import ch.jalu.wordlehelper.model.Cell;
import ch.jalu.wordlehelper.model.Color;
import ch.jalu.wordlehelper.model.WordleResultData;
import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.MinimumCountPredicate;
import ch.jalu.wordlehelper.util.HashSetMultimap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ch.jalu.wordlehelper.util.CollectionUtil.invertMap;
import static ch.jalu.wordlehelper.util.CollectionUtil.retainTopKeys;

public final class SplitterWordFinder {

    private SplitterWordFinder() {
    }

    public static TreeMap<BigDecimal, List<String>> split(List<String> allWords, WordleResultData resultData,
                                                          WordleTurnEvaluator turnEvaluator, boolean tryAllWords) {
        List<String> possibleResults = allWords.stream()
            .filter(resultData::matches)
            .toList();

        if (tryAllWords) {
            return split0(allWords, possibleResults, resultData, turnEvaluator);
        }
        return split0(possibleResults, possibleResults, resultData, turnEvaluator);
    }

    public static TreeMap<BigDecimal, List<String>> split0(List<String> wordsToTry, List<String> possibleResults,
                                                       WordleResultData resultData, WordleTurnEvaluator turnEvaluator) {
        if (possibleResults.size() > 50) {
            throw new IllegalStateException("Cannot have more than 50 possible results");
        }

        Map<String, BigDecimal> countByWord = new HashMap<>();
        for (String word : wordsToTry) {
            long matches = 0;
            for (String result : possibleResults) {
                List<Cell> cells = turnEvaluator.evaluateCells(word, result);
                WordleResultData newResultData = newDataWithCells(resultData, cells);

                matches += (possibleResults.stream().filter(newResultData::matches).count() - 1);
            }
            countByWord.put(word, BigDecimal.valueOf(matches));
        }

        return retainTopKeys(invertMap(countByWord), 20);
    }

    private static WordleResultData newDataWithCells(WordleResultData currentData, List<Cell> cells) {
        Character[] newKnownCharsByIndex = currentData.knownCharactersByIndex().clone();
        Map<Character, CharCountPredicate> newPredicatesByChar = new HashMap<>();
        HashSetMultimap<Integer, Character> oldWrongCharsByIndex = currentData.wrongCharsByIndex();
        HashSetMultimap<Integer, Character> copyWrongCharsByIndex = null;

        for (int i = 0; i < cells.size(); ++i) {
            Cell cell = cells.get(i);
            if (cell.color() == Color.GREEN) {
                newKnownCharsByIndex[i] = cell.character();
            } else  if (cell.color() == Color.GRAY) {
                if (copyWrongCharsByIndex != null || !oldWrongCharsByIndex.contains(i, cell.character())) {
                    if (copyWrongCharsByIndex == null) {
                        copyWrongCharsByIndex = copyHashSetMultimap(oldWrongCharsByIndex);
                    }
                    copyWrongCharsByIndex.put(i, cell.character());
                }
            }

            CharCountPredicate newPredicate = newPredicatesByChar.computeIfAbsent(
                    cell.character(), v -> MinimumCountPredicate.of(0)).update(cell.color());
            newPredicatesByChar.put(cell.character(), newPredicate);
        }

        currentData.predicatesByChar().forEach((chr, pred) -> {
            newPredicatesByChar.merge(chr, pred, CharCountPredicate::merge);
        });

        return new WordleResultData(
                newKnownCharsByIndex,
                (copyWrongCharsByIndex == null ? oldWrongCharsByIndex : copyWrongCharsByIndex),
                newPredicatesByChar);
    }

    private static HashSetMultimap<Integer, Character> copyHashSetMultimap(HashSetMultimap<Integer, Character> original) {
        HashSetMultimap<Integer, Character> copy = new HashSetMultimap<>();
        original.getBackingMap().forEach((key, values) -> {
            values.forEach(value -> copy.put(key, value));
        });
        return copy;
    }
}
