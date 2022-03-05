package ch.jalu.wordlehelper;

import ch.jalu.wordlehelper.evaluation.LetterFrequencyCalculator;
import ch.jalu.wordlehelper.evaluation.WordleTurnEvaluator;
import ch.jalu.wordlehelper.util.FileUtil;
import ch.jalu.wordlehelper.util.ListSortedMultimap;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;
import static ch.jalu.wordlehelper.util.CollectionUtil.invertMap;
import static ch.jalu.wordlehelper.util.CollectionUtil.retainTopKeys;

public class StartWordEvaluator {

    private final LetterFrequencyCalculator letterFrequencyCalculator;
    private final WordleTurnEvaluator wordleTurnEvaluator;

    StartWordEvaluator(LetterFrequencyCalculator letterFrequencyCalculator,
                       WordleTurnEvaluator wordleTurnEvaluator) {
        this.letterFrequencyCalculator = letterFrequencyCalculator;
        this.wordleTurnEvaluator = wordleTurnEvaluator;
    }

    public static void main(String[] args) {
        StartWordEvaluator evaluator = new StartWordEvaluator(new LetterFrequencyCalculator(), new WordleTurnEvaluator());
        evaluator.evaluate();
    }

    private void evaluate() {
        List<String> starterWords  = FileUtil.readWordFileAndSort(Paths.get("words.txt"));
        Set<String> allWords       = FileUtil.readWordFileAsSet(Paths.get("all_words.txt"));
        List<String> pastResults   = FileUtil.readWordFileAsList(Paths.get("past_results.txt"));
        System.out.println("Read " + starterWords.size() + " starter words, " + allWords.size() + " total words, "
            + pastResults.size() + " result words");

        Map<Character, BigDecimal> charFrequencyStarters    = letterFrequencyCalculator.calculateFrequencyOfLetters(starterWords);
        Map<Character, BigDecimal> charFrequencyAll         = letterFrequencyCalculator.calculateFrequencyOfLetters(allWords);
        Map<Character, BigDecimal> charFrequencyPastResults = letterFrequencyCalculator.calculateFrequencyOfLetters(pastResults);
        System.out.println(invertMap(charFrequencyStarters));
        System.out.println(invertMap(charFrequencyAll));
        System.out.println(invertMap(charFrequencyPastResults));

        Map<String, BigDecimal> frequencyScoreStarters    = scoreWordsByLetterFrequency(charFrequencyStarters, starterWords);
        Map<String, BigDecimal> frequencyScoreAll         = scoreWordsByLetterFrequency(charFrequencyAll, starterWords);
        Map<String, BigDecimal> frequencyScorePastResults = scoreWordsByLetterFrequency(charFrequencyPastResults, starterWords);
        System.out.println(retainTopKeys(invertMap(frequencyScoreStarters), 10));
        System.out.println(retainTopKeys(invertMap(frequencyScoreAll), 10));
        System.out.println(retainTopKeys(invertMap(frequencyScorePastResults), 10));

//        TreeMap<BigDecimal, List<String>> wordPairs = evaluator.scoreWordPairs(distroStarters, words);
//        retainTopKeys(wordPairs, 10);
//        System.out.println(wordPairs);


        TreeMap<BigDecimal, List<String>> infoScoreStarters = scoreByInfo(starterWords, starterWords);
        TreeMap<BigDecimal, List<String>> infoScoreAll = scoreByInfo(starterWords, allWords);
        TreeMap<BigDecimal, List<String>> infoScorePastResults = scoreByInfo(starterWords, pastResults);
        System.out.println(retainTopKeys(infoScoreStarters, 10));
        System.out.println(retainTopKeys(infoScoreAll, 10));
        System.out.println(retainTopKeys(infoScorePastResults, 10));
    }

    private void checkWordsExist(List<String> myWords, Set<String> allWords) {
        for (String word : myWords) {
            if (!allWords.contains(word)) {
                throw new IllegalArgumentException(word);
            }
        }
    }

    private Map<String, BigDecimal> scoreWordsByLetterFrequency(Map<Character, BigDecimal> frequencyMap, List<String> words) {
        Map<String, BigDecimal> scoreByWord = new HashMap<>();
        for (String word : words) {
            BigDecimal score = BigDecimal.ZERO;
            for (int i = 0; i < WORD_LENGTH; ++i) {
                score = score.add(frequencyMap.get(word.charAt(i)));
            }
            scoreByWord.put(word, score);
        }
        return scoreByWord;
    }

    private TreeMap<BigDecimal, List<String>> scoreWordPairs(Map<Character, BigDecimal> score,
                                                             List<String> words) {
        ListSortedMultimap<BigDecimal, String> pairsByScore = new ListSortedMultimap<>();
        for (int i = 0; i < words.size(); ++i) {
            String word1 = words.get(i);
            for (int j = i + 1; j < words.size(); ++j) {
                String word2 = words.get(j);
                BigDecimal pairScore = Stream.concat(asCharStream(word1), asCharStream(word2))
                    .distinct()
                    .map(score::get)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                pairsByScore.put(pairScore, word1 + "," + word2);
            }
        }
        return pairsByScore.getBackingMap();
    }

    private static Stream<Character> asCharStream(String string) {
        return string.chars()
            .mapToObj(chr -> (char) chr);
    }

    private TreeMap<BigDecimal, List<String>> scoreByInfo(Collection<String> starterWords,
                                                          Collection<String> allWords) {
        ListSortedMultimap<BigDecimal, String> wordsByScore = new ListSortedMultimap<>();
        for (String myWord : starterWords) {
            BigDecimal score = BigDecimal.ZERO;
            for (String allWord : allWords) {
                score = score.add(wordleTurnEvaluator.calculateScore(myWord, allWord));
            }
            wordsByScore.put(score, myWord);
        }
        return wordsByScore.getBackingMap();
    }
}
