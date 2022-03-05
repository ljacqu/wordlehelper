package ch.jalu.wordlehelper.util;

import static ch.jalu.wordlehelper.Constants.ENABLE_TIMER;

public class Timer {

    private final String prefix;
    private long start;

    public Timer() {
        this("");
    }

    public Timer(String prefix) {
        start = System.nanoTime();
        this.prefix = prefix;
    }

    public void start() {
        start = System.nanoTime();
    }

    public void log(String text) {
        if (ENABLE_TIMER) {
            long diff = (System.nanoTime() - start) / 1000;
            System.out.println(prefix + "[" + diff + "] " + text);
            start = System.nanoTime();
        }
    }
}
