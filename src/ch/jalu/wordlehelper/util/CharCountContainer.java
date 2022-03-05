package ch.jalu.wordlehelper.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CharCountContainer {

    private final Map<Character, Integer> charCount = new HashMap<>();

    public void add(char chr) {
        charCount.merge(chr, 1, Integer::sum);
    }

    public int getCount(char chr) {
        Integer result = charCount.get(chr);
        return result == null ? 0 : result;
    }

    public Integer getCount(Character chr) {
        Integer result = charCount.get(chr);
        return result == null ? 0 : result;
    }

    public void clear() {
       charCount.clear();
    }

    public boolean isEmpty() {
        return charCount.isEmpty();
    }

    public void forEach(BiConsumer<Character, Integer> consumer) {
        charCount.forEach(consumer);
    }

    public void subtract(CharCountContainer otherCounter) {
        otherCounter.charCount.forEach((chr, count) -> {
            int thisCount = this.getCount(chr);
            if (thisCount > 0) {
                this.charCount.put(chr, Math.max(thisCount - count, 0));
            }
        });
    }

    public void merge(CharCountContainer otherCounter) {
        otherCounter.charCount.forEach((chr, count) -> {
            if (this.getCount(chr) < count) {
                this.charCount.put(chr, count);
            }
        });
    }

    public boolean hasSameCountsOrMore(CharCountContainer otherCounter) {
        return otherCounter.charCount.entrySet().stream()
            .allMatch(entry -> this.getCount(entry.getKey()) >= entry.getValue());
    }
}
