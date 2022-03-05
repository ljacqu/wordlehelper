package ch.jalu.wordlehelper.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractMultimap<K, V, C extends Collection<V>, M extends Map<K, C>> {

    private final M backingMap;

    public AbstractMultimap(M backingMap) {
        this.backingMap = backingMap;
    }

    public M getBackingMap() {
        return backingMap;
    }

    public void put(K key, V value) {
        backingMap.computeIfAbsent(key, k -> newCollection())
            .add(value);
    }

    public boolean containsKey(K key) {
        return backingMap.containsKey(key);
    }

    public C get(K key) {
        return backingMap.get(key);
    }

    public boolean contains(K key, V value) {
        C values = backingMap.get(key);
        return values != null && values.contains(value);
    }

    public void forEach(BiConsumer<K, C> consumer) {
        backingMap.forEach(consumer);
    }

    protected abstract C newCollection();
}
