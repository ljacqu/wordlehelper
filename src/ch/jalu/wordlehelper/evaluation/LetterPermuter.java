package ch.jalu.wordlehelper.evaluation;

import ch.jalu.wordlehelper.Constants;
import ch.jalu.wordlehelper.model.WordleResultData;
import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.HasExactCountPredicate;
import ch.jalu.wordlehelper.model.predicate.MinimumCountPredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LetterPermuter {

    private LetterPermuter() {
    }

    public static List<String> generateAllCombinations(WordleResultData resultData) {
        Map<Integer, Character> knownCharsByIndex = collectKnownCharsByIndex(resultData);
        List<Character> yellowChars = collectYellowChars(resultData);

        List<Character> permutationElements = new ArrayList<>(Constants.WORD_LENGTH);
        permutationElements.addAll(yellowChars);
        for (int i = permutationElements.size(); i < Constants.WORD_LENGTH - knownCharsByIndex.size(); ++i) {
            permutationElements.add('*');
        }

        // Example: (Wordle #1000)
        //   B  E? E  T? S
        //   D  U  V  E? T!
        // results in knownCharsByIndex={4=T} permutationElements=[E, *, *, *]

        Map<Integer, Integer> permutationIndexToWordIndex =
                mapPermutationIndexToWordIndex(permutationElements, knownCharsByIndex);

        List<String> permutations = createPermutations(
                "", permutationElements, permutationIndexToWordIndex, resultData);
        return permutations.stream()
            .map(permutation -> addKnownCharactersAndSpacesToPermutation(permutation, knownCharsByIndex))
            .sorted()
            .toList();
    }

    private static Map<Integer, Integer> mapPermutationIndexToWordIndex(List<Character> permutationElements,
                                                                        Map<Integer, Character> knownCharsByIndex) {
        Map<Integer, Integer> permutationIndexToWordIndex = new HashMap<>();
        int wordIndex = 0;
        for (int i = 0; i < permutationElements.size(); i++) {
            while (knownCharsByIndex.containsKey(wordIndex)) {
                ++wordIndex;
            }
            permutationIndexToWordIndex.put(i, wordIndex);
            ++wordIndex;
        }
        return Collections.unmodifiableMap(permutationIndexToWordIndex);
    }

    private static List<String> createPermutations(String prefix,
                                                   List<Character> elements,
                                                   Map<Integer, Integer> permutationIndexToWordIndex,
                                                   WordleResultData resultData) {
        if (elements.isEmpty()) {
            return List.of(prefix);
        }

        final int currentPermutationIndex = prefix.length();
        final int wordIndex = permutationIndexToWordIndex.get(currentPermutationIndex);


        List<String> result = new ArrayList<>();
        elements.stream()
            .distinct()
            .filter(element -> !resultData.wrongCharsByIndex().contains(wordIndex, element))
            .forEach(element -> {
                List<Character> newElements = new ArrayList<>(elements);
                newElements.remove(element);
                String newPrefix = prefix + element;
                result.addAll(createPermutations(newPrefix, newElements, permutationIndexToWordIndex, resultData));
            });
        return result;
    }

    private static String addKnownCharactersAndSpacesToPermutation(String permutation,
                                                                   Map<Integer, Character> knownCharsByIndex) {
        String completePattern;
        if (knownCharsByIndex.isEmpty()) {
            completePattern = permutation;
        } else {
            completePattern = "";
            int permutationIndex = 0;
            for (int i = 0; i < Constants.WORD_LENGTH; ++i) {
                Character knownChar = knownCharsByIndex.get(i);
                if (knownChar != null) {
                    completePattern += knownChar;
                } else {
                    completePattern += permutation.charAt(permutationIndex);
                    ++permutationIndex;
                }
            }
        }

        StringBuilder sb = new StringBuilder(completePattern.length() * 2 - 1);
        for (int i = 0; i < completePattern.length(); ++i) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(completePattern.charAt(i));
        }
        return sb.toString();
    }

    private static Map<Integer, Character> collectKnownCharsByIndex(WordleResultData resultData) {
        Map<Integer, Character> knownCharsByIndex = new HashMap<>(Constants.WORD_LENGTH);
        Character[] knownCharactersByIndex = resultData.knownCharactersByIndex();
        for (int i = 0; i < knownCharactersByIndex.length; i++) {
            if (knownCharactersByIndex[i] != null) {
                knownCharsByIndex.put(i, knownCharactersByIndex[i]);
            }
        }
        return Collections.unmodifiableMap(knownCharsByIndex);
    }

    /**
     * Returns a list of all characters we know must be present, but whose location is not known. The same
     * character may appear multiple times in the list (when appropriate).
     *
     * @param resultData result data to process
     * @return list of letters whose position is unknown but which must be part of the solution
     */
    private static List<Character> collectYellowChars(WordleResultData resultData) {
        List<Character> yellowChars = new ArrayList<>();
        resultData.predicatesByChar().forEach((chr, pred) -> {
            for (int i = 0; i < getCharCountPredicateCount(pred); ++i) {
                yellowChars.add(chr);
            }
        });

        for (Character knownChar : resultData.knownCharactersByIndex()) {
            if (knownChar != null) {
                yellowChars.remove(knownChar);
            }
        }

        return yellowChars;
    }

    private static int getCharCountPredicateCount(CharCountPredicate predicate) {
        if (predicate instanceof MinimumCountPredicate mcp) {
            return mcp.getMinimumCount();
        } else if (predicate instanceof HasExactCountPredicate hecp) {
            return hecp.getRequiredCount();
        } else {
            throw new IllegalStateException("Unhandled predicate: " + predicate.getClass());
        }
    }
}
