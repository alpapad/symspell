package com.faroo.test.perf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.faroo.test.perf.algo.sp3.SymSpell3WordSearch2;
import com.google.common.base.Stopwatch;
import com.google.common.math.StatsAccumulator;

public class WordSearchComparison2 {

    // private static String fullTestData = "src/main/resources/full_test.txt";
    // private static String fullTestData = "src/main/resources/f1.txt";
    // private static String fullTestData = "src/test/resources/cen.txt";
    private static String testData = "src/test/resources/test_data/error_corpus.csv";

    // private static String testSet1 = "src/main/resources/spell-testset1.txt";
    // private static String testSet2 = "src/main/resources/spell-testset2.txt";
    // private static String testSet3 = "src/main/resources/spell-testset3.txt";
    // private static String cesnsus = "src/main/resources/Names_2010Census.csv";
    // very verbose!!
    private static boolean printFailures = true;

    // private static boolean acceptSecondHitAsSuccess = true;

    static float wrdSize = 0;
    static float wrdCnt = 0;
    static float hitCount = 0;

    static Map<Integer, StatsAccumulator> overSized = new LinkedHashMap<>();
    static Map<Integer, StatsAccumulator> failed = new LinkedHashMap<>();

    static void gc() {
        // System.err.println("\n Forcing GC");
        for (int i = 0; i < 2; i++) {
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }

    // 2 695 724
    public static void main(String[] args) throws IOException, InterruptedException {
        WordSearch2[] wordCorrectImplementations = { //
                new SymSpell3WordSearch2(2), //
                // new BkWordSearch(2), //
                // new SymSpell3WordSearch(2), //
                // new SymSpell3WordSearch(2), //
                // new SymSpell3WordSearch(2), //
                // new SymSpell6WordSearch(2), //

                // new SymSpell6WordSearch(2), //
                // new SymSpell6WordSearch(2), //
                new SymSpell3WordSearch2(2), //

                // new SymSpell6WordSearch(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                new SymSpell3WordSearch2(2), //
                // new SymSpell7WordSearch(2), //
                // new SymSpell7WordSearch(2), //
                // new SymSpell6WordSearch(2),
                // new SymSpell6WordSearch(2),
                // new BkWordSearch(2), //
                // new DamerauWordSearch(2), //
                // new LevenshteinWordSearch(2), //
                // new SymSpell3WordSearch(2),
                // new SymSpell3WordSearch(2),
                // new SymSpell3WordSearch(2),
                // new SymSpell3WordSearch(2),
                // new SymSpell7WordSearch(2),
                // new SymSpell7WordSearch(2),
                // new SymSpell7WordSearch(2),

                // new SymSpell3WordSearch(2),
                // new SymSpell6WordSearch(2),
                // PreDictFactory.getCommunityEdition(),

                // works only if the dependency is on the classpath
                // PreDictFactory.getEnterpriseEdition(AccuracyLevel.fast),

                // new LuceneWordSearch(),

                // This impl. is worse then the Lucene one, but it should be
                // comparable, since there's also Lucene inside.
                // Maybe it's bad, because not optimally implemented.
                // new ElasticsearchWordSearch()
        };

        for (int k = 0; k < wordCorrectImplementations.length; k++) {
            gc();
            WordSearch2 wordCorrect = wordCorrectImplementations[k];
            overSized.clear();
            failed.clear();
            for (int i = 1; i < 100; i++) {
                overSized.put(Integer.valueOf(i), new StatsAccumulator());
                failed.put(Integer.valueOf(i), new StatsAccumulator());
            }
            String name = wordCorrect.toString();
            System.out.println("\n----------------------------------------------");
            System.out.println("              " + name);
            System.out.println("----------------------------------------------");

            run(wordCorrect);
            // release mem
            wordCorrectImplementations[k] = null;
            System.out.println();
            System.out.println("Total Stats");
            DecimalFormat df = new DecimalFormat("#####.#######");
            df.setRoundingMode(RoundingMode.CEILING);

            for (Entry<Integer, StatsAccumulator> e : overSized.entrySet()) {
                StatsAccumulator acc = e.getValue();
                if (acc.count() > 0) {
                    try {
                        if (acc.count() > 1) {
                            System.out.println(String.format("%,4d", e.getKey()) //
                                    + "; count:" + String.format("%,6d", acc.count())//
                                    + "; sum:" + String.format("%,12.0f", acc.sum()) //
                                    + "; mean:" + String.format("%,12.3f", acc.mean()) //
                                    + "; Stdev:" + String.format("%,12.3f", acc.sampleStandardDeviation()) //
                                    + "; var:" + String.format("%,12.3f", acc.sampleVariance()));
                        } else {
                            System.out.println(String.format("%,4d", e.getKey()) //
                                    + "; count:" + String.format("%,6d", acc.count())//
                                    + "; sum:" + String.format("%,12.0f", acc.sum()));
                        }
                    } catch (Exception exx) {
                        exx.printStackTrace();
                    }
                }
            }

            System.out.println("Failed queries stats");
            for (Entry<Integer, StatsAccumulator> e : failed.entrySet()) {
                StatsAccumulator acc = e.getValue();
                if (acc.count() > 0) {
                    try {
                        if (acc.count() > 1) {
                            System.out.println(String.format("%,4d", e.getKey()) //
                                    + "; count:" + String.format("%,6d", acc.count())//
                                    + "; sum:" + String.format("%,12.0f", acc.sum()) //
                                    + "; mean:" + String.format("%,12.3f", acc.mean()) //
                                    + "; Stdev:" + String.format("%,12.3f", acc.sampleStandardDeviation()) //
                                    + "; var:" + String.format("%,12.3f", acc.sampleVariance()));
                        } else {
                            System.out.println(String.format("%,4d", e.getKey()) //
                                    + "; count:" + String.format("%,6d", acc.count())//
                                    + "; sum:" + String.format("%,12.0f", acc.sum()));
                        }
                    } catch (Exception exx) {
                        exx.printStackTrace();
                    }
                }
            }
            System.out.println();
        }
    }

    public static void run(WordSearch2 wordCorrect) throws IOException {
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        CSVParser parser = CSVParser.parse(new File(testData), Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(','));

        Map<String, String> tpCandidates = new HashMap<>();
        Map<String, String> fpCandidates = new HashMap<>();

        // index
        System.out.println("Loading tests..");
        Stopwatch stopWatch = Stopwatch.createUnstarted();
        stopWatch.start();
        int indexCount = 0;
        Iterator<CSVRecord> csvIterator = parser.iterator();
        csvIterator.next();

        while (csvIterator.hasNext()) {
            CSVRecord csvRecord = csvIterator.next();
            int dist = Integer.valueOf(csvRecord.get(2));

            if (dist < 3) {
                appendToList(tpCandidates, csvRecord);
            } else {
                if (csvRecord.get(1).equals(csvRecord.get(0))) {
                    System.out.println("WRONG: " + csvRecord.get(1) + "," + csvRecord.get(0) + ",false");
                }
                appendToList(fpCandidates, csvRecord);
            }

            // wordCorrect.indexWord(csvRecord.get(0));
            indexCount++;
            if (indexCount % 1000 == 1) {
                System.out.print(".");
            }
            if (indexCount % 80000 == 1 && indexCount > 80000) {
                System.out.println("");
            }

        }
        System.out.println("");
        System.out.println("Loading index");
        wordCorrect.loadIndex("src/test/resources/test_data/test_corpus.idx");
        Duration indexTime = stopWatch.stop().elapsed();
        System.out.println("Done ");
        gc();
        System.out.println("Memory used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem) / 1024 / 1024 + "Mb");

        stopWatch.reset().start();

        // for each spellTestSetEntry do all searches
        int success = 0;
        int fail = 0;
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        int count = 0;

        // final AtomicInteger m = new AtomicInteger(0);
        Map<Integer, List<Integer>> res;

        res = tpCandidates.entrySet().stream().map((candidate) -> {
            // int h = m.incrementAndGet();
            // if (h % 1000 == 1) {
            // System.err.print(".");
            // }
            // if (h % 80000 == 1 && h > 80000) {
            // System.err.println("");
            // }
            List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
            if (isMatch(candidate, results)) {
                return 1;
            } else {
                if (printFailures) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(": '" + candidate.getValue() + "' not found by search for '" + candidate.getKey() + "' ");
                    if (results.size() > 0)
                        sb.append(" found ");
                    results.forEach((r) -> {
                        sb.append("'" + r + "',");
                    });
                    System.out.println(sb.toString());
                }
                return 0;
            }
        }).collect(Collectors.groupingBy((a) -> a));

        if (res.get(Integer.valueOf(1)) != null) {
            success += res.get(Integer.valueOf(1)).size();
            truePositives += res.get(Integer.valueOf(1)).size();
            count += res.get(Integer.valueOf(1)).size();
        }
        if (res.get(Integer.valueOf(0)) != null) {
            fail += res.get(Integer.valueOf(0)).size();
            falseNegatives += res.get(Integer.valueOf(0)).size();
            count += res.get(Integer.valueOf(0)).size();
        }
        // int i = 0;
        // for (Entry<String, String> candidate : tpCandidates.entrySet()) {
        // List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
        // i++;
        // // first or second match count as success
        // if (isMatch(candidate, results)) {
        // success++;
        // truePositives++;
        // } else {
        // if (printFailures) {
        // System.out.println(
        // count + ": '" + candidate.getValue() + "' not found by search for " + candidate.getKey());
        // if (results.size() > 0)
        // System.out.print(" found '");
        // results.forEach((r) -> {
        // System.out.print("'" + r + "',");
        // });
        // System.out.println();
        // }
        // fail++;
        // falseNegatives++;
        // }
        // count++;
        // // if (i >= 1_000) {
        // // break;
        // // }
        // }

        res = fpCandidates.entrySet().stream().map((candidate) -> {
            List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
            if (isNotMatch(candidate, results)) {
                return 0;
            } else {
                if (printFailures) {
                    System.out.println("false-positive: found for: '" + candidate.getValue() + "' by search for " + candidate.getKey());
                    if (results.size() > 0)
                        System.out.print("   found:");
                    results.forEach((r) -> {
                        System.out.print("'" + r + "',");
                    });
                    System.out.println();
                }
                return 1;
            }
        }).collect(Collectors.groupingBy((a) -> a));

        if (res.get(Integer.valueOf(1)) != null) {
            fail += res.get(Integer.valueOf(1)).size();
            falsePositives += res.get(Integer.valueOf(1)).size();
            count += res.get(Integer.valueOf(1)).size();
        }
        if (res.get(Integer.valueOf(0)) != null) {
            success += res.get(Integer.valueOf(0)).size();
            trueNegatives += res.get(Integer.valueOf(0)).size();
            count += res.get(Integer.valueOf(0)).size();
        }

        // for (Entry<String, String> candidate : fpCandidates.entrySet()) {
        // List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
        //
        // // first or second match count as success
        // if (isMatch(candidate, results) &&
        // !candidate.getKey().equals(results.get(0))) {
        // fail++;
        // falsePositives++;
        // if (printFailures) {
        // System.out.println("false-positive: found '" + results.get(0) + "' by search
        // for '" + candidate
        // .getKey() + "'");
        // if (results.size() > 1 && acceptSecondHitAsSuccess) {
        // System.out.println(" + found '" + results.get(1) + "' as well'");
        // }
        // System.out.println();
        // }
        // } else {
        // success++;
        // trueNegatives++;
        // }
        // count++;
        // }

        Duration elapsed = stopWatch.stop().elapsed();

        System.out.println("indexed " + indexCount + " words in " + indexTime.toMillis() + "ms");
        System.out.println(count + " searches");
        System.out.println(elapsed.toMillis() + "ms => " + String.format("%1$10.3f searches/ms", ((double) count / (elapsed.toMillis()))));
        System.out.println();
        System.out.println(success + " success / accuracy => " + String.format("%8.2f%%", (100.0 * success / count)));
        System.out.println(truePositives + " true-positives");
        System.out.println(trueNegatives + " true-negatives");
        System.out.println();
        System.out.println(fail + " fail => " + String.format("%8.2f%%", (100.0 * fail / count)));
        System.out.println(falseNegatives + " false-negatives");
        System.out.println(falsePositives + " false-positives");
        System.out.println();
        if (wordCorrect instanceof Closeable) {
            ((Closeable) wordCorrect).close();
        }
    }

    private static void appendToList(Map<String, String> tpCandidates, CSVRecord csvRecord) {
        String lookup = csvRecord.get(1);
        String correct = csvRecord.get(0);
        if (correct.trim().indexOf(' ') < 0 && lookup.trim().indexOf(' ') < 0) {
            tpCandidates.put(correct.trim(), lookup.trim());
        }
    }

    /*
     * static int wrdSize =0; static int hitCount = 0;
     */

    private static boolean isMatch(Entry<String, String> candidate, List<String> results) {

        overSized.get(Integer.valueOf(candidate.getValue().length())).add(results.size());
        boolean found = results.stream().filter(r -> r.equals(candidate.getValue())).findFirst().isPresent();
        if (!found) {
            failed.get(Integer.valueOf(candidate.getValue().length())).add(results.size());
        }
        return found;

    }

    private static boolean isNotMatch(Entry<String, String> candidate, List<String> results) {
        overSized.get(Integer.valueOf(candidate.getValue().length())).add(results.size());
        boolean found = results.stream().filter(r -> r.equals(candidate.getValue())).findFirst().isPresent();
        if (found) {
            failed.get(Integer.valueOf(candidate.getValue().length())).add(results.size());
        }
        return !found;
    }

}
