package ch.jalu.wordlehelper;

import ch.jalu.wordlehelper.evaluation.GameDataCreator;
import ch.jalu.wordlehelper.evaluation.LetterFrequencyCalculator;
import ch.jalu.wordlehelper.evaluation.WordleTurnEvaluator;
import ch.jalu.wordlehelper.model.Cell;
import ch.jalu.wordlehelper.model.Color;
import ch.jalu.wordlehelper.model.Turn;
import ch.jalu.wordlehelper.model.WordleResultData;
import ch.jalu.wordlehelper.model.predicate.CharCountPredicate;
import ch.jalu.wordlehelper.model.predicate.HasExactCountPredicate;
import ch.jalu.wordlehelper.model.predicate.MinimumCountPredicate;
import ch.jalu.wordlehelper.util.CharCountContainer;
import ch.jalu.wordlehelper.util.ConsoleGamePrinter;
import ch.jalu.wordlehelper.util.FileUtil;
import ch.jalu.wordlehelper.util.Timer;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ch.jalu.wordlehelper.Constants.WEIGHT_CHANGED_YELLOW;
import static ch.jalu.wordlehelper.Constants.WEIGHT_MIN_TO_EXACT;
import static ch.jalu.wordlehelper.Constants.WEIGHT_NEW_EXACT;
import static ch.jalu.wordlehelper.Constants.WEIGHT_NEW_FULL_EXCLUSION;
import static ch.jalu.wordlehelper.Constants.WEIGHT_NEW_YELLOW;
import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;
import static ch.jalu.wordlehelper.util.CollectionUtil.combineMaps;
import static ch.jalu.wordlehelper.util.CollectionUtil.groupByNormalizedValueDescending;
import static ch.jalu.wordlehelper.util.CollectionUtil.retainTopKeys;

public class TurnEvaluator {

    private final GameDataCreator gameDataCreator;
    private final WordleTurnEvaluator wordleTurnEvaluator;
    private final LetterFrequencyCalculator letterFrequencyCalculator;

    private final Timer timer = new Timer();
    private final List<String> allWords;
    private final List<Turn> turns = new ArrayList<>();

    TurnEvaluator(GameDataCreator gameDataCreator, WordleTurnEvaluator wordleTurnEvaluator,
                  LetterFrequencyCalculator letterFrequencyCalculator, List<String> allWords) {
        this.gameDataCreator = gameDataCreator;
        this.wordleTurnEvaluator = wordleTurnEvaluator;
        this.letterFrequencyCalculator = letterFrequencyCalculator;
        this.allWords = List.copyOf(allWords);
    }

    public static void main(String... args) {
        List<String> allWords = FileUtil.readWordFileAsList(Paths.get("all_words.txt"));
        TurnEvaluator evaluator = new TurnEvaluator(new GameDataCreator(),
            new WordleTurnEvaluator(), new LetterFrequencyCalculator(), allWords);
        evaluator.run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Input: ");
                String line = scanner.nextLine().trim();
                if ("exit".equals(line)) {
                    break;
                } else if ("pop".equals(line)) {
                    if (!turns.isEmpty()) {
                        turns.remove(turns.size() - 1);
                    }
                    ConsoleGamePrinter.printGameToConsole(turns);
                } else if ("help".equals(line)) {
                    System.out.println("exit - stop game");
                    System.out.println("pop  - remove last saved turn");
                    System.out.println("new  - clear all turns");
                    System.out.println("run  - run evaluation again");
                    System.out.println("half - find out which word will most likely halve the set of possible words");
                } else if ("new".equals(line)) {
                    turns.clear();
                    System.out.println("Removed all turns");
                } else if ("run".equals(line)) {
                    runAndCatchExceptionWithHelpHint(() -> evaluate(turns));
                } else if ("half".equals(line)) {
                    runAndCatchExceptionWithHelpHint(this::findBestWordsForHalving);
                } else if (!line.isEmpty()) {
                    runAndCatchExceptionWithHelpHint(() -> {
                        turns.add(Turn.of(line.toUpperCase()));
                        evaluate(turns);
                    });
                }
            }
        }
    }

    private static void runAndCatchExceptionWithHelpHint(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Type 'help' for help");
        }
    }

    private void evaluate(List<Turn> turns) {
        if (turns.isEmpty()) {
            throw new IllegalStateException("Need at least one turn! Use SOARE or ARISE (or run "
                + StartWordEvaluator.class.getSimpleName() + " to find out good starter words)");
        }
        timer.start();
        System.out.println("Processing game:");
        ConsoleGamePrinter.printGameToConsole(turns);

        WordleResultData resultData = gameDataCreator.constructResultData(turns);
        System.out.println("Open letters:");
        ConsoleGamePrinter.printLetterInfoToConsole(resultData.knownCharactersByIndex(), resultData.predicatesByChar());

        List<String> possibleWords = allWords.stream()
            .filter(resultData::matches)
            .collect(Collectors.toCollection(ArrayList::new));
        Set<String> possibleWordsSet = Set.copyOf(possibleWords);

        System.out.println();
        System.out.println("Found " + possibleWords.size() + " possible words:");
        System.out.println(" " + String.join(", ", possibleWords));
        timer.log("Print console & find possible words");

        if (possibleWords.size() < 2) {
            return;
        }

        System.out.println();
        System.out.println("Scoring by new information contents:");
        NewInfoResult newInfoResult = scoreByNewInfo(resultData, possibleWords);
        timer.log("Calc new info contents");
        System.out.println("(weighted) " + newInfoResult.weighted());
        System.out.println("(not weighted): " + newInfoResult.unweighted());
        System.out.println("Possible results (weighted): " + newMapWithFilteredValues(newInfoResult.weighted(), possibleWordsSet));
        timer.log("Print new info contents");

        System.out.println();
        System.out.println("Weighted by potential cell colors:");
        TreeMap<BigDecimal, List<String>> scoredByAllPossibleWords = scoreByInfo(possibleWords, possibleWords);
        timer.log("Calc new cell color info");
        System.out.println("(all possibilities): " + scoredByAllPossibleWords);

        List<String> pastResults = FileUtil.readWordFileAsList(Paths.get("past_results.txt"));
        List<String> stillPossiblePastResults = pastResults.stream()
            .filter(possibleWordsSet::contains)
            .toList();
        TreeMap<BigDecimal, List<String>> scoredByPossiblePastResults = scoreByInfo(possibleWords, stillPossiblePastResults);
        System.out.println("(past results): " + scoredByPossiblePastResults);
        System.out.println("(combined): " + combineMaps(scoredByAllPossibleWords, scoredByPossiblePastResults));
    }

    private void findBestWordsForHalving() {
        WordleResultData resultData = gameDataCreator.constructResultData(turns);
        List<String> possibleWords = allWords.stream()
            .filter(resultData::matches)
            .collect(Collectors.toCollection(ArrayList::new)); // Concrete List type so it is guaranteed to implement RandomAccess

        if (possibleWords.size() < 300 && possibleWords.size() > 2) {
            System.out.println();
            System.out.println("Scoring by word most likely to split in half the set of possible words:");
            TreeMap<BigDecimal, List<String>> scoreByRemainingWords = scoreByRemainingWords(allWords, possibleWords);
            retainTopKeys(scoreByRemainingWords, 10);
            System.out.println(scoreByRemainingWords);
        } else {
            throw new IllegalStateException("Found " + possibleWords.size() + " possible words, which is not within bounds for this action");
        }
    }

    private TreeMap<BigDecimal, List<String>> scoreByInfo(Collection<String> givenWords,
                                                          Collection<String> referenceWords) {
        BigDecimal maxScore = BigDecimal.ZERO;
        Map<String, BigDecimal> scoresByWord = new HashMap<>(givenWords.size());
        for (String givenWord : givenWords) {
            BigDecimal score = BigDecimal.ZERO;
            for (String referenceWord : referenceWords) {
                score = score.add(wordleTurnEvaluator.calculateScore(givenWord, referenceWord));
            }
            scoresByWord.put(givenWord, score);
            maxScore = maxScore.compareTo(score) > 0 ? maxScore : score;
        }
        return groupByNormalizedValueDescending(maxScore, scoresByWord);
    }

    private Predicate<String> createPredicate(String word, String result) {
        Predicate<Character>[] predicates = new Predicate[WORD_LENGTH];

        GameDataCreator.CharCountPredicateBuilder colorCounter = new GameDataCreator.CharCountPredicateBuilder();
        // todo possible to not go via Cells but immediately have logic in here?
        List<Cell> cells = wordleTurnEvaluator.evaluateCells(word, result);
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            char character = cell.character();
            Color color = cell.color();
            colorCounter.register(character, color);

            if (cell.color() == Color.GREEN) {
                predicates[i] = (chr -> chr == character);
            } else {
                predicates[i] = (chr -> chr != character);
            }
        }

        Map<Character, CharCountPredicate> predicatesByChar = colorCounter.getPredicatesByChar();
        Predicate<String> predicate = inspectedWord -> {
            // Interestingly, using CharCountContainer in here instead of a Map directly seems to perform slightly better
            CharCountContainer charCount = new CharCountContainer();
            for (int i = 0; i < WORD_LENGTH; ++i) {
                char chr = inspectedWord.charAt(i);
                if (!predicates[i].test(chr)) {
                    return false;
                }
                charCount.add(chr);
            }
            for (Map.Entry<Character, CharCountPredicate> entry : predicatesByChar.entrySet()) {
                Integer count = charCount.getCount(entry.getKey());
                if (!entry.getValue().matches(count)) {
                    return false;
                }
            }
            return true;
        };
        return predicate;
    }

    private NewInfoResult scoreByNewInfo(WordleResultData wordleResultData, List<String> possibleWords) {
        Timer timer = new Timer(" > newInfo ");
        Map<Character, BigDecimal> frequencyByChar =
            letterFrequencyCalculator.calculateFrequencyOfLetters(possibleWords, wordleResultData.predicatesByChar());
        timer.log("Calc frequency");

        Map<String, BigDecimal> scoresByWord = new HashMap<>();
        Map<String, BigDecimal> weightedScoresByWord = new HashMap<>();

        for (String potentialNextPlay : allWords) {
            BigDecimal score = BigDecimal.ZERO;
            BigDecimal weightedScore = BigDecimal.ZERO;
            for (String potentialResult : possibleWords) {
                BigDecimal[] scores = evaluateNewInformation(wordleResultData, potentialNextPlay, potentialResult, frequencyByChar);
                score = score.add(scores[0]);
                weightedScore = weightedScore.add(scores[1]);
            }
            scoresByWord.put(potentialNextPlay, score);
            weightedScoresByWord.put(potentialNextPlay, weightedScore);
        }
        timer.log("Eval of words");

        BigDecimal maxScore = scoresByWord.values().stream()
            .max(Comparator.comparing(v -> v))
            .orElse(null);
        BigDecimal maxWeightedScore = weightedScoresByWord.values().stream()
            .max(Comparator.comparing(Function.identity()))
            .orElse(null);

        return new NewInfoResult(groupByNormalizedValueDescending(maxScore, scoresByWord),
            groupByNormalizedValueDescending(maxWeightedScore, weightedScoresByWord));
    }

    private record NewInfoResult(TreeMap<BigDecimal, List<String>> unweighted,
                                 TreeMap<BigDecimal, List<String>> weighted) {
    }

    private TreeMap<BigDecimal, List<String>> newMapWithFilteredValues(Map<BigDecimal, List<String>> map,
                                                                       Set<String> wordsToFilter) {
        TreeMap<BigDecimal, List<String>> result = new TreeMap<>(Collections.reverseOrder());
        map.forEach((score, values) -> {
            List<String> filteredValues = values.stream().filter(wordsToFilter::contains).toList();
            if (!filteredValues.isEmpty()) {
                result.put(score, filteredValues);
            }
        });
        return result;
    }

    private BigDecimal[] evaluateNewInformation(WordleResultData resultData, String word, String result,
                                                Map<Character, BigDecimal> frequencyByChar) {
        List<Cell> cells = wordleTurnEvaluator.evaluateCells(word, result);
        GameDataCreator.CharCountPredicateBuilder colorCounter = new GameDataCreator.CharCountPredicateBuilder();
        for (Cell cell : cells) {
            colorCounter.register(cell.character(), cell.color());
        }

        BigDecimal score = BigDecimal.ZERO;
        BigDecimal scoreWeighted = BigDecimal.ZERO;
        for (Map.Entry<Character, CharCountPredicate> entry : colorCounter.getPredicatesByChar().entrySet()) {
            Character character = entry.getKey();
            CharCountPredicate newPredicate = entry.getValue();
            CharCountPredicate oldPredicate = resultData.predicatesByChar().get(character);
            if (newPredicate != oldPredicate) {
                BigDecimal charFrequency = frequencyByChar.getOrDefault(character, BigDecimal.ZERO);
                if (newPredicate instanceof HasExactCountPredicate newExactPredicate) {
                    if (newExactPredicate.getRequiredCount() == 0) {
                        score = score.add(WEIGHT_NEW_FULL_EXCLUSION);
                        scoreWeighted = scoreWeighted.add(WEIGHT_NEW_FULL_EXCLUSION.multiply(charFrequency));
                    } else if (oldPredicate instanceof MinimumCountPredicate) {
                        score = score.add(WEIGHT_MIN_TO_EXACT);
                        scoreWeighted = scoreWeighted.add(WEIGHT_MIN_TO_EXACT.multiply(charFrequency));
                    } else {
                        score = score.add(WEIGHT_NEW_EXACT);
                        scoreWeighted = scoreWeighted.add(WEIGHT_NEW_EXACT.multiply(charFrequency));
                    }

                } else if (newPredicate instanceof MinimumCountPredicate newMinPredicate) {
                    if (oldPredicate instanceof MinimumCountPredicate oldMinPredicate) {
                        if (oldMinPredicate.getMinimumCount() < newMinPredicate.getMinimumCount()) {
                            score = score.add(WEIGHT_CHANGED_YELLOW);
                            scoreWeighted = scoreWeighted.add(WEIGHT_CHANGED_YELLOW.multiply(charFrequency));
                        }
                    } else if (oldPredicate == null) {
                        score = score.add(WEIGHT_NEW_YELLOW);
                        scoreWeighted = scoreWeighted.add(WEIGHT_NEW_YELLOW.multiply(charFrequency));
                    }
                }
            }
        }
        return new BigDecimal[]{
            score, scoreWeighted
        };
    }

    private TreeMap<BigDecimal, List<String>> scoreByRemainingWords(Collection<String> allWords,
                                                                    List<String> possibleWords) {
        /// todo skip the words of the loop and the previous words...
        Map<String, BigDecimal> scoresByWord = new HashMap<>();
        final int totalPossibleWords = possibleWords.size();
        final double halfPossibleWords = possibleWords.size() / 2.0;
        int processedWords = 0;
        for (String potentialNextPlay : allWords) {
            double differencesTotal = 0.0;
            int wordMatches;
            for (int i = 0; i < totalPossibleWords; ++i) {
                String potentialResult = possibleWords.get(i);
                wordMatches = 0;
                Predicate<String> matchesResultPred = createPredicate(potentialNextPlay, potentialResult);

                for (int j = 0; j < totalPossibleWords; ++j) {
                    if (matchesResultPred.test(possibleWords.get(j))) {
                        ++wordMatches;
                    }
                }
                differencesTotal += Math.abs(wordMatches - halfPossibleWords);
            }

            double score = halfPossibleWords - (differencesTotal / totalPossibleWords);
            scoresByWord.put(potentialNextPlay, BigDecimal.valueOf(score));

            ++processedWords;
            if ((processedWords & 127) == 127) {
                System.out.print(". ");
            }
        }
        System.out.println();
        return groupByNormalizedValueDescending(BigDecimal.valueOf(halfPossibleWords), scoresByWord);
    }
}
