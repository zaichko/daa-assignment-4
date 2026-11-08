package textanalysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    static final String ALPHABET = "abcd";
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUPS = 2;
    static final int PATTERN_LEN = 20;

    static volatile long sink;

    static String randomString(Random rnd, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    static double avgMicros(Runnable task) {
        for (int i = 0; i < WARMUPS; i++) task.run();
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            task.run();
            total += System.nanoTime() - start;
        }
        return total / (double) RUNS / 1000.0;
    }

    public static void main(String[] args) throws IOException {
        runSuite();
        List<List<String>> tables = runSuite();

        Files.createDirectories(Path.of("results"));
        write("results/dp.csv", tables.get(0));
        write("results/matching.csv", tables.get(1));
        write("results/kasai.csv", tables.get(2));
        for (List<String> rows : tables) {
            rows.forEach(System.out::println);
            System.out.println();
        }
    }

    static List<List<String>> runSuite() {
        Random rnd = new Random(SEED);
        List<String> dpCsv = new ArrayList<>(), matchCsv = new ArrayList<>(), kasaiCsv = new ArrayList<>();
        dpCsv.add("algorithm,n,avg_us,cell_updates");
        matchCsv.add("algorithm,text_len,pattern,avg_us,char_comparisons,rk_false_matches,occurrences");
        kasaiCsv.add("n,avg_us,char_comparisons");

        // ---------------------------------------------------- LCS / Edit Distance
        for (int n : new int[]{100, 500, 1000}) {
            String a = randomString(rnd, n), b = randomString(rnd, n);

            Lcs.cellUpdates = 0;
            Lcs.length(a, b);
            long lcsOps = Lcs.cellUpdates;
            double lcsUs = avgMicros(() -> sink = Lcs.length(a, b));
            dpCsv.add(String.format(Locale.ROOT, "LCS,%d,%.1f,%d", n, lcsUs, lcsOps));

            EditDistance.cellUpdates = 0;
            EditDistance.distance(a, b);
            long edOps = EditDistance.cellUpdates;
            double edUs = avgMicros(() -> sink = EditDistance.distance(a, b));
            dpCsv.add(String.format(Locale.ROOT, "EditDistance,%d,%.1f,%d", n, edUs, edOps));
        }

        // ----------------------------------------------------- String matching
        for (int n : new int[]{10_000, 50_000, 100_000}) {
            String text = randomString(rnd, n);
            String present = text.substring(n / 2, n / 2 + PATTERN_LEN);
            String absent;
            do {
                absent = randomString(rnd, PATTERN_LEN);
            } while (!StringMatching.naive(text, absent).isEmpty());

            String[] names = {"present", "absent"};
            String[] patterns = {present, absent};
            for (int p = 0; p < 2; p++) {
                String pat = patterns[p];

                String[] algo = {"Naive", "KMP", "RabinKarp"};
                List<Integer> naiveRes = StringMatching.naive(text, pat);
                List<Integer> kmpRes = StringMatching.kmp(text, pat);
                List<Integer> rkRes = StringMatching.rabinKarp(text, pat);
                if (!naiveRes.equals(kmpRes) || !kmpRes.equals(rkRes)) {
                    throw new IllegalStateException("Algorithms disagree for n=" + n + " " + names[p]);
                }
                for (String name : algo) {
                    Runnable task = switch (name) {
                        case "Naive" -> () -> sink = StringMatching.naive(text, pat).size();
                        case "KMP" -> () -> sink = StringMatching.kmp(text, pat).size();
                        default -> () -> sink = StringMatching.rabinKarp(text, pat).size();
                    };
                    StringMatching.resetCounters();
                    task.run();
                    long comparisons = StringMatching.comparisons;
                    long falseMatches = StringMatching.falseMatches;
                    double us = avgMicros(task);
                    matchCsv.add(String.format(Locale.ROOT, "%s,%d,%s,%.1f,%d,%d,%d",
                            name, n, names[p], us, comparisons, falseMatches, naiveRes.size()));
                }
            }
        }

        // -------------------------------------------------------------- Kasai
        for (int n : new int[]{1000, 5000, 10_000}) {
            String s = randomString(rnd, n);
            int[] sa = Kasai.simpleSuffixArray(s); // untimed input preparation
            Kasai.comparisons = 0;
            Kasai.buildLcp(s, sa);
            long comparisons = Kasai.comparisons;
            double us = avgMicros(() -> sink = Kasai.buildLcp(s, sa).length);
            kasaiCsv.add(String.format(Locale.ROOT, "%d,%.1f,%d", n, us, comparisons));
        }

        return List.of(dpCsv, matchCsv, kasaiCsv);
    }

    private static void write(String file, List<String> lines) throws IOException {
        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            lines.forEach(out::println);
        }
    }
}
