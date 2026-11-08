package textanalysis;

import java.util.ArrayList;
import java.util.List;

public final class StringMatching {
    public static long comparisons;
    public static long falseMatches;

    private StringMatching() {}

    public static void resetCounters() {
        comparisons = 0;
        falseMatches = 0;
    }

    // ---------------------------------------------------------------- Naive
    public static List<Integer> naive(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return result;
        for (int i = 0; i + m <= n; i++) {
            int j = 0;
            while (j < m) {
                comparisons++;
                if (text.charAt(i + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) result.add(i);
        }
        return result;
    }

    // ------------------------------------------------------------------ KMP
    static int[] buildLps(String p) {
        int m = p.length();
        int[] lps = new int[m];
        int len = 0;
        for (int i = 1; i < m; i++) {
            while (true) {
                comparisons++;
                if (p.charAt(i) == p.charAt(len)) {
                    len++;
                    break;
                }
                if (len == 0) break;
                len = lps[len - 1];
            }
            lps[i] = len;
        }
        return lps;
    }

    public static List<Integer> kmp(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return result;
        int[] lps = buildLps(pattern);
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (true) {
                comparisons++;
                if (text.charAt(i) == pattern.charAt(j)) {
                    j++;
                    break;
                }
                if (j == 0) break;
                j = lps[j - 1];
            }
            if (j == m) {
                result.add(i - m + 1);
                j = lps[j - 1];
            }
        }
        return result;
    }

    // ------------------------------------------------------------ Rabin-Karp
    private static final long BASE = 256;
    private static final long MOD = 1_000_000_007L;

    public static List<Integer> rabinKarp(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return result;

        long highPow = 1;
        for (int i = 0; i < m - 1; i++) highPow = (highPow * BASE) % MOD;

        long patHash = 0, winHash = 0;
        for (int i = 0; i < m; i++) {
            patHash = (patHash * BASE + pattern.charAt(i)) % MOD;
            winHash = (winHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i + m <= n; i++) {
            if (patHash == winHash) {
                int j = 0;
                while (j < m) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) break;
                    j++;
                }
                if (j == m) result.add(i);
                else falseMatches++;
            }
            if (i + m < n) {
                winHash = (winHash - text.charAt(i) * highPow % MOD + MOD) % MOD;
                winHash = (winHash * BASE + text.charAt(i + m)) % MOD;
            }
        }
        return result;
    }
}
