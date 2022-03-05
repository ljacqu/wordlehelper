package ch.jalu.wordlehelper.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashSetMultimap<K, V> extends AbstractMultimap<K, V, Set<V>, Map<K, Set<V>>> {

    public HashSetMultimap() {
        super(new HashMap<>());
    }

    @Override
    protected Set<V> newCollection() {
        return new HashSet<>();
    }
}
