package me.cleardragonf.com.util;

public final class RomanNumerals {
    private RomanNumerals() {}

    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] NUMERALS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        int n = number;
        for (int i = 0; i < VALUES.length; i++) {
            while (n >= VALUES[i]) {
                n -= VALUES[i];
                sb.append(NUMERALS[i]);
            }
        }
        // If number was extremely large, append 'M' for leftovers beyond classical range
        // (though practical levels won't exceed a few thousand typically)
        while (n >= 1000) {
            sb.append('M');
            n -= 1000;
        }
        return sb.toString();
    }
}

