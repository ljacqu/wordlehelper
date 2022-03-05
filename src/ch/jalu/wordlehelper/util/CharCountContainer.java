package ch.jalu.wordlehelper.util;

import java.util.HashMap;
import java.util.Map;

public class CharCountContainer {

    private final Map<Character, Integer> charCount = new HashMap<>();

    public void add(char chr) {
        charCount.merge(chr, 1, Integer::sum);
    }

    public Integer getCount(Character chr) {
        Integer result = charCount.get(chr);
        return result == null ? 0 : result;
    }
}
