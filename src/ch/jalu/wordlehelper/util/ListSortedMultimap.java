package ch.jalu.wordlehelper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class ListSortedMultimap<K extends Comparable<K>, V>
    extends AbstractMultimap<K, V, List<V>, TreeMap<K, List<V>>> {

    public ListSortedMultimap() {
        super(new TreeMap<>());
    }

    private ListSortedMultimap(TreeMap<K, List<V>> map) {
        super(map);
    }

    public static <K extends Comparable<K>, V> ListSortedMultimap<K, V> withReverseOrder() {
        // Note: IntelliJ displays the type args as redundant,
        // but the compiler complains that it cannot infer them
        return new ListSortedMultimap<K, V>(new TreeMap<K, List<V>>(Collections.reverseOrder()));
    }

    @Override
    protected List<V> newCollection() {
        return new ArrayList<>();
    }
}
