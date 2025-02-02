package ch.jalu.wordlehelper;

import java.math.BigDecimal;

public final class Constants {

    public static final boolean USE_PAST_RESULTS = false;

    public static final boolean ENABLE_TIMER = false;

    public static final BigDecimal WEIGHT_NEW_EXACT          = new BigDecimal("1");
    public static final BigDecimal WEIGHT_MIN_TO_EXACT       = new BigDecimal("0.5");
    public static final BigDecimal WEIGHT_NEW_FULL_EXCLUSION = new BigDecimal("0.1");
    public static final BigDecimal WEIGHT_NEW_YELLOW         = new BigDecimal("1");
    public static final BigDecimal WEIGHT_CHANGED_YELLOW     = new BigDecimal("0.5");
    public static final BigDecimal WEIGHT_NEW_GREEN_CELL     = new BigDecimal("0.25");

    public static final int WORD_LENGTH = 5;

    private Constants() {
    }
}
