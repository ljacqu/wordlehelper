package ch.jalu.wordlehelper.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static <K extends Comparable<K>, V> TreeMap<K, V> retainTopKeys(TreeMap<K, V> map, int nBestToKeep) {
        var keyset = map.descendingKeySet().iterator();
        int i = 0;
        K value = null;
        while (keyset.hasNext() && i < nBestToKeep) {
            value = keyset.next();
            ++i;
        }
        final K threshold = value;

        map.entrySet().removeIf(entry -> entry.getKey().compareTo(threshold) < 0);
        return map;
    }

    public static <K, V extends Comparable<V>> TreeMap<V, List<K>> invertMap(Map<K, V> map) {
        ListSortedMultimap<V, K> result = new ListSortedMultimap<>();
        map.forEach((key, value) -> result.put(value, key));
        return result.getBackingMap();
    }

    public static <V> TreeMap<BigDecimal, List<V>> groupByNormalizedValueDescending(BigDecimal maxValue,
                                                                                    Map<V, BigDecimal> map) {
        MathContext mathContext = new MathContext(2);
        Function<BigDecimal, BigDecimal> normalizer = (maxValue == null || maxValue.compareTo(BigDecimal.ZERO) == 0)
            ? Function.identity()
            : (score -> score.divide(maxValue, mathContext).setScale(2, RoundingMode.HALF_UP));

        ListSortedMultimap<BigDecimal, V> normalizedMap = ListSortedMultimap.withReverseOrder();
        map.forEach((value, score) -> {
            normalizedMap.put(normalizer.apply(score), value);
        });
        return normalizedMap.getBackingMap();
    }

    @SafeVarargs
    public static <V> TreeMap<BigDecimal, List<V>> combineMaps(Map<BigDecimal, List<V>>... scoreMaps) {
        if (scoreMaps.length < 2) {
            throw new IllegalArgumentException("Need at least two maps to combine");
        }

        Map<V, BigDecimal> scoresByValue = new HashMap<>();
        Arrays.stream(scoreMaps)
            .flatMap(map -> map.entrySet().stream())
            .forEach((Map.Entry<BigDecimal, List<V>> entry) -> {
                BigDecimal score = entry.getKey();
                entry.getValue().forEach(value -> {
                    BigDecimal currentScore = scoresByValue.getOrDefault(value, BigDecimal.ZERO);
                    scoresByValue.put(value, currentScore.add(score));
                });
            });

        return groupByNormalizedValueDescending(null, scoresByValue);
    }
}
