package ch.jalu.wordlehelper.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HashSetMultimap<K, V> extends AbstractMultimap<K, V, Set<V>, Map<K, Set<V>>> {

    public HashSetMultimap() {
        super(new HashMap<>());
    }

    private HashSetMultimap(Map<K, Set<V>> map) {
        super(map);
    }

    public HashSetMultimap<K, V> copy() {
        Map<K, Set<V>> copy = getBackingMap().entrySet().stream()
            .map(entry -> Map.entry(entry.getKey(), new HashSet<>(entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new HashSetMultimap<>(copy);
    }

    @Override
    protected Set<V> newCollection() {
        return new HashSet<>();
    }
}
