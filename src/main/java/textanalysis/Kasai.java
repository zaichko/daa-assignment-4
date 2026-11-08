package textanalysis;

import java.util.Arrays;

public final class Kasai {
    public static long comparisons;
    private Kasai() {}

    public static int[] buildLcp(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int k = sa[rank[i] - 1];
                while (true) {
                    if (i + h >= n || k + h >= n) break;
                    comparisons++;
                    if (s.charAt(i + h) != s.charAt(k + h)) break;
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        return lcp;
    }


    public static int[] simpleSuffixArray(String s) {
        int n = s.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> compareSuffixes(s, a, b));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) sa[i] = idx[i];
        return sa;
    }

    private static int compareSuffixes(String s, int a, int b) {
        int n = s.length();
        while (a < n && b < n) {
            char x = s.charAt(a++), y = s.charAt(b++);
            if (x != y) return x < y ? -1 : 1;
        }
        if (a == n && b == n) return 0;
        return a == n ? -1 : 1;
    }
}
