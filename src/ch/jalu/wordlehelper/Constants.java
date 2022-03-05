package ch.jalu.wordlehelper;

import java.math.BigDecimal;

public final class Constants {

    public static final int WORD_LENGTH = 5;

    public static final boolean ENABLE_TIMER = true;

    public static final BigDecimal WEIGHT_NEW_EXACT          = new BigDecimal("1");
    public static final BigDecimal WEIGHT_MIN_TO_EXACT       = new BigDecimal("0.05");
    public static final BigDecimal WEIGHT_NEW_FULL_EXCLUSION = new BigDecimal("0.1");
    public static final BigDecimal WEIGHT_NEW_YELLOW         = new BigDecimal("0.8");
    public static final BigDecimal WEIGHT_CHANGED_YELLOW     = new BigDecimal("0.2");

    private Constants() {
    }
}
