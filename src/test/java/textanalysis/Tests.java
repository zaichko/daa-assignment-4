package textanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Dependency-free test runner: prints PASS/FAIL per check, exits 1 on any failure. */
public class Tests {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }

    public static void main(String[] args) {
        lcsTests();
        editDistanceTests();
        matchingTests();
        kasaiTests();
        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ LCS
    private static void lcsTests() {
        check("LCS identical strings", Lcs.length("abcde", "abcde") == 5);
        check("LCS completely different strings", Lcs.length("abc", "xyz") == 0);
        check("LCS empty string (first)", Lcs.length("", "abc") == 0);
        check("LCS empty string (second)", Lcs.length("abc", "") == 0);
        check("LCS classic example AGGTAB/GXTXAYB", Lcs.length("AGGTAB", "GXTXAYB") == 4);
    }

    // -------------------------------------------------------- Edit distance
    private static void editDistanceTests() {
        check("Edit identical strings", EditDistance.distance("abc", "abc") == 0);
        check("Edit empty -> abc", EditDistance.distance("", "abc") == 3);
        check("Edit abc -> empty", EditDistance.distance("abc", "") == 3);
        check("Edit substitution cat->cut", EditDistance.distance("cat", "cut") == 1);
        check("Edit insertion cat->cart", EditDistance.distance("cat", "cart") == 1);
        check("Edit deletion cart->cat", EditDistance.distance("cart", "cat") == 1);
        check("Edit kitten->sitting", EditDistance.distance("kitten", "sitting") == 3);
    }

    // ------------------------------------------------------ String matching
    private static void matchCase(String name, String text, String pattern, Integer... expected) {
        List<Integer> exp = Arrays.asList(expected);
        List<Integer> a = StringMatching.naive(text, pattern);
        List<Integer> b = StringMatching.kmp(text, pattern);
        List<Integer> c = StringMatching.rabinKarp(text, pattern);
        check("Naive: " + name, a.equals(exp));
        check("KMP: " + name, b.equals(exp));
        check("Rabin-Karp: " + name, c.equals(exp));
        check("All three agree: " + name, a.equals(b) && b.equals(c));
    }

    private static void matchingTests() {
        matchCase("pattern at beginning", "abcdefgh", "abc", 0);
        matchCase("pattern in middle", "xxabcxx", "abc", 2);
        matchCase("pattern at end", "xxxxabc", "abc", 4);
        matchCase("multiple occurrences", "abababab", "abab", 0, 2, 4);
        matchCase("overlapping occurrences", "aaaa", "aa", 0, 1, 2);
        matchCase("pattern absent", "abcdef", "xyz");
        matchCase("pattern longer than text", "ab", "abc");
        matchCase("empty pattern (returns empty list)", "abc", "");

        // Random cross-check against an independent reference (String.startsWith).
        Random rnd = new Random(1);
        boolean allOk = true;
        for (int iter = 0; iter < 500; iter++) {
            String text = random(rnd, rnd.nextInt(60), "ab");
            String pat = random(rnd, rnd.nextInt(6), "ab");
            List<Integer> ref = new ArrayList<>();
            if (!pat.isEmpty()) {
                for (int i = 0; i + pat.length() <= text.length(); i++) {
                    if (text.startsWith(pat, i)) ref.add(i);
                }
            }
            if (!ref.equals(StringMatching.naive(text, pat))
                    || !ref.equals(StringMatching.kmp(text, pat))
                    || !ref.equals(StringMatching.rabinKarp(text, pat))) {
                allOk = false;
            }
        }
        check("500 random text/pattern pairs: Naive, KMP, Rabin-Karp match reference", allOk);
    }

    // ---------------------------------------------------------------- Kasai
    private static void kasaiTests() {
        // "banana": suffixes sorted:
        //   5 a | 3 ana | 1 anana | 0 banana | 4 na | 2 nana
        // LCP of neighbours: -, a/ana=1, ana/anana=3, anana/banana=0, banana/na=0, na/nana=2
        String s = "banana";
        int[] sa = {5, 3, 1, 0, 4, 2};
        int[] expectedLcp = {0, 1, 3, 0, 0, 2};
        check("Kasai banana: simple suffix array equals manual one",
                Arrays.equals(Kasai.simpleSuffixArray(s), sa));
        check("Kasai banana: LCP equals manual one", Arrays.equals(Kasai.buildLcp(s, sa), expectedLcp));

        // Cross-check against brute-force LCP on random strings.
        Random rnd = new Random(2);
        boolean allOk = true;
        for (int iter = 0; iter < 200; iter++) {
            String t = random(rnd, 1 + rnd.nextInt(40), "abc");
            int[] tsa = Kasai.simpleSuffixArray(t);
            int[] expected = new int[t.length()];
            for (int i = 1; i < t.length(); i++) {
                int a = tsa[i - 1], b = tsa[i], h = 0;
                while (a + h < t.length() && b + h < t.length() && t.charAt(a + h) == t.charAt(b + h)) h++;
                expected[i] = h;
            }
            if (!Arrays.equals(expected, Kasai.buildLcp(t, tsa))) allOk = false;
        }
        check("Kasai: 200 random strings match brute-force LCP", allOk);
    }

    private static String random(Random rnd, int n, String alphabet) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }
}
