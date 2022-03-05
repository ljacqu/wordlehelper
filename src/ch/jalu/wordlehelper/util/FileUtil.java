package ch.jalu.wordlehelper.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import static ch.jalu.wordlehelper.Constants.WORD_LENGTH;

public final class FileUtil {

    private FileUtil() {
    }

    public static List<String> readWordFileAsList(Path path) {
        return readWordFile(path, ArrayList::new);
    }

    public static Set<String> readWordFileAsSet(Path path) {
        return readWordFile(path, HashSet::new);
    }

    public static List<String> readWordFileAndSort(Path path) {
        List<String> words = readWordFileAsList(path);
        Collections.sort(words);
        return words;
    }

    private static <C extends Collection<String>> C readWordFile(Path path, IntFunction<C> collectionCreator) {
        List<String> readLines;
        try {
            readLines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        C words = collectionCreator.apply(readLines.size());
        for (String line : readLines) {
            if (!line.isEmpty()) {
                if (line.length() != WORD_LENGTH) {
                    throw new IllegalStateException("Expected " + WORD_LENGTH + " chars but got '" + line + "'");
                }
                words.add(line.toUpperCase());
            }
        }
        return words;
    }
}
