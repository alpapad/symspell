package com.faroo.test.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v3.SymSpellV3;
import com.faroo.symspell.impl.v6.SuggestionStage;
import com.faroo.symspell.impl.v6.SymSpellV6;
import com.google.common.base.Stopwatch;
import com.ibm.icu.text.Transliterator;

public class Benchmark {
/*
 * Run params: 
 * -XX:+UseG1GC -Xms10G -XX:StringTableSize=1000003 -XX:+PrintFlagsFinal -verbose:gc -XX:+PrintStringTableStatistics
 * 
 * -XX:StringTableSize=10000019 // 10mil
 */
    static String path = "src/test/resources/test_data";
    // static String errors = path + "/noisy_query_en_1000.txt";
    // static String errors = path + "/batch0.tab";
    static String query1k = path + "/errors.txt";
    static String[] dictionaryPath = { //
            path + "/frequency_dictionary_en_30_000.txt", //
            path + "/frequency_dictionary_en_82_765.txt", //
            path + "/frequency_dictionary_en_500_000.txt" };
  
    static String[] dictionaryName = { "30k", "82k", "500k" };

    static int[] dictionarySize = { 29159, 82765, 500000 };

    static String id = "Any-Latin; nfd; [:nonspacing mark:] remove; nfc";
    static Transliterator ts = Transliterator.getInstance(id);
    static DecimalFormat df = new DecimalFormat("0000000000.00");
    static DecimalFormat ft = new DecimalFormat("0000000000.00");
    static DecimalFormat pct = new DecimalFormat("000.00");
    
    static DecimalFormat iint = new DecimalFormat("000000000000");
    
    static Pattern pt = Pattern.compile("^([0]+).*");
    static  String zToSpace(String in) {
    	Matcher m = pt.matcher(in);
    	if(m.matches()) {
    		String pre = m.group(1); 
    		StringBuilder sb = new StringBuilder();
    		for(int i =0; i< pre.length(); i++) {
    			sb.append(" ");
    		}
    		sb.append(in.substring(pre.length()));
    		return sb.toString();
    	} else {
    		return in;
    	}
    }
    static {
        df.setRoundingMode(RoundingMode.CEILING);
        ft.setRoundingMode(RoundingMode.CEILING);
        pct.setRoundingMode(RoundingMode.CEILING);
        iint.setRoundingMode(RoundingMode.CEILING);

    }

    static String PCT(double number) {
        return (zToSpace(pct.format(number * 100)) + "%");
    }
    
    static String MB(double number) {
        return (zToSpace(df.format(number)) + "Mb");
    }
    
    static String FT(double number) {
        return zToSpace(ft.format(number));
    }
    
    static String INT(Number number) {
        return zToSpace(iint.format(number));
    }
    
    static void gc() {
        for (int i = 0; i < 2; i++) {
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
    static void gc1() {
        for (int i = 0; i < 2; i++) {
            System.gc();
        }
    }
    // load 1000 terms with random spelling errors
    static List<String> buildQuery1K() throws FileNotFoundException, IOException {
        List<String> testList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(query1k))) {
            String line;
            // process a single line at a time only for memory efficiency
            while ((line = br.readLine()) != null) {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length >= 2) {
                    if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
                        testList.add(ts.transform(lineParts[0].toLowerCase().trim().replaceAll("\"", "")));
                    }
                }
            }
        }
        return testList;
    }

    // pre-run to ensure code has executed once before timing benchmarks
    static void warmUp() throws FileNotFoundException, IOException {
        SymSpellV3 dict = new SymSpellV3();
        loadDictionary(dict, dictionaryPath[0]);
        dict.lookup("hockie", Verbosity.All, 1);

        SymSpellV6 dictOrig = new SymSpellV6(2);
        loadDictionary(dictOrig, dictionaryPath[0]);

        dictOrig.lookup("hockie", Verbosity.All, 1);
    }

    static long getTotalMemory() {
        gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    static void benchmarkPrecalculationLookup() throws FileNotFoundException, IOException {
        List<String> query1k = buildQuery1K();
        int resultNumber = 0;
        double repetitions = 1000;
        int totalLoopCount = 0;
        long totalMatches = 0;
        long totalOrigMatches = 0;
        double totalLoadTime, totalMem, totalLookupTime, totalOrigLoadTime, totalOrigMem, totalOrigLookupTime;
        totalLoadTime = totalMem = totalLookupTime = totalOrigLoadTime = totalOrigMem = totalOrigLookupTime = 0;
        double totalRepetitions = 0;

        Stopwatch stopWatch = Stopwatch.createStarted();
        for (int maxEditDistance = 2; maxEditDistance < 3; maxEditDistance++) {

            // benchmark dictionary precalculation size and time
            // maxEditDistance=1/2/3; prefixLength=5/6/7; dictionary=30k/82k/500k;
            // class=instantiated/static
            for (int i = 0; i < dictionaryPath.length; i++) {
                totalLoopCount++;

                // instantiated dictionary
                long memSize = getTotalMemory();
                memSize = getTotalMemory();
                stopWatch.reset().start();
                SymSpellV3 dict = new SymSpellV3(maxEditDistance);
                loadDictionary(dict, dictionaryPath[i]);
                stopWatch.stop();
                long memDelta = getTotalMemory() - memSize;

                totalLoadTime += stopWatch.elapsed().getSeconds();
                totalMem += memDelta / 1024.0 / 1024.0;
                System.out.println("Precalculation v3 " + INT(stopWatch.elapsed().getSeconds()) + "s " + MB(memDelta / 1024.0 / 1024.0) + " " + INT(dict.getWordCount()) 
                + " words " + INT(dict.getEntryCount())
                        + " entries  MaxEditDistance=" + maxEditDistance + " dict=" + dictionaryName[i]);

                // static dictionary
                memSize = getTotalMemory();
                stopWatch.reset().start();
                SymSpellV6 dictOrig = new SymSpellV6(maxEditDistance);
                loadDictionary(dictOrig, dictionaryPath[i]);
                // LoadDictionary(dictOrig, DictionaryPath[i]);
                totalOrigLoadTime += stopWatch.stop().elapsed().getSeconds();

                memDelta = getTotalMemory() - memSize;
                totalOrigMem += memDelta / 1024.0 / 1024.0;

                System.out.println("Precalculation v6 " + INT(stopWatch.elapsed().getSeconds()) + "s " 
                + MB(memDelta / 1024 / 1024.0) + " " + INT(dictOrig.getWordCount()) + " words " + INT(dictOrig.getEntryCount())
                        + " entries  MaxEditDistance=" + maxEditDistance + " dict=" + dictionaryName[i]);

                // benchmark lookup result number and time
                // maxEditDistance=1/2/3; prefixLength=5/6/7; dictionary=30k/82k/500k;
                // verbosity=0/1/2; query=exact/non-exact/mix; class=instantiated/static
                for (Verbosity verbosity : new Verbosity[] { Verbosity.All }) { // Verbosity.values()) {
                    // instantiated exact
                    stopWatch.reset().start();
                    for (int round = 0; round < repetitions; round++) {
                        resultNumber = dict.lookup("different", verbosity, maxEditDistance).size();
                    }

                    totalLookupTime += stopWatch.stop().elapsed().toNanos();
                    gc1();
                    totalMatches += resultNumber;
                    System.out.println("lookup v3 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity + " query=exact");
                    // static exact
                    stopWatch.reset().start();
                    for (int round = 0; round < repetitions; round++) {
                        resultNumber = dictOrig.lookup("different", Verbosity.valueOf(verbosity.name()), maxEditDistance).size();
                    }
                    totalOrigLookupTime += stopWatch.stop().elapsed().toNanos();
                    totalOrigMatches += resultNumber;
                    gc1();
                    System.out.println("lookup v6 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity + " query=exact");
                    System.out.println();
                    totalRepetitions += repetitions;

                    // instantiated non-exact
                    stopWatch.reset().start();
                    for (int round = 0; round < repetitions; round++) {
                        resultNumber = dict.lookup("hockie", verbosity, maxEditDistance).size();
                    }
                    totalLookupTime += stopWatch.stop().elapsed().toNanos();
                    gc1();
                    totalMatches += resultNumber;
                    System.out.println("lookup v3 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity + " query=non-exact");
                    // static non-exact
                    stopWatch.reset().start();
                    for (int round = 0; round < repetitions; round++) {
                        resultNumber = dictOrig.lookup("hockie", verbosity, maxEditDistance).size();
                    }
                    totalOrigLookupTime += stopWatch.stop().elapsed().toNanos();
                    gc1();
                    totalOrigMatches += resultNumber;
                    System.out.println("lookup v6 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity + " query=non-exact");
                    System.out.println();
                    totalRepetitions += repetitions;

                    // instantiated mix
                    stopWatch.reset().start();
                    resultNumber = 0;
                    for (String word : query1k) {
                        resultNumber += dict.lookup(word, verbosity, maxEditDistance).size();
                    }

                    totalLookupTime += stopWatch.stop().elapsed().toNanos();
                    totalMatches += resultNumber;
                    gc1();
                    
                    System.out.println("lookup v3 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / (double) query1k.size()) + "ns/op verbosity=" + verbosity + " query=mix");
                    // static mix
                    stopWatch.reset().start();
                    resultNumber = 0;
                    for (String word : query1k) {
                        resultNumber += dictOrig.lookup(word, verbosity, maxEditDistance).size();
                    }

                    totalOrigLookupTime += stopWatch.stop().elapsed().toNanos();
                    totalOrigMatches += resultNumber;
                    gc1();
                    System.out.println("lookup v6 " + INT(resultNumber) + " results " + FT(stopWatch.elapsed().toNanos() / (double) query1k.size()) + "ns/op verbosity=" + verbosity + " query=mix");
                    System.out.println();
                    totalRepetitions += query1k.size();
                    System.out.println("lookup v3 acc: " + " ---> " + INT(dict.acc.count())//
                            + " sum:" + FT(dict.acc.sum()) //
                            + "  mean:" + FT(dict.acc.mean())//
                            + ", Stdev:" + FT(dict.acc.sampleStandardDeviation()) //
                            + ", var:" + FT(dict.acc.sampleVariance()));

                    System.out.println("lookup v3 dur: " + " ---> " + INT(dict.du.count())//
                            + " sum:" + FT(dict.du.sum() / 1000) //
                            + "  mean:" + FT(dict.du.mean())//
                            + ", Stdev:" + FT(dict.du.sampleStandardDeviation()) //
                            + ", var:" + FT(dict.du.sampleVariance()));

                    System.out.println("lookup v6 acc: " + " ---> " + INT(dictOrig.acc.count())//
                            + " sum:" + FT(dictOrig.acc.sum()) //
                            + "  mean:" + FT(dictOrig.acc.mean())//
                            + ", Stdev:" + FT(dictOrig.acc.sampleStandardDeviation()) //
                            + ", var:" + FT(dictOrig.acc.sampleVariance()));

                    System.out.println("lookup v3 dur: " + " ---> " + INT(dictOrig.du.count())//
                            + " sum:" + FT(dictOrig.du.sum() / 1000) //
                            + "  mean:" + FT(dictOrig.du.mean())//
                            + ", Stdev:" + FT(dictOrig.du.sampleStandardDeviation()) //
                            + ", var:" + FT(dictOrig.du.sampleVariance()));
                    System.out.println();
                }
                System.out.println();

                dict = null;
                dictOrig = null;
            }
        }
        System.out.println("Average Precalculation time v3 " + FT(totalLoadTime / totalLoopCount) + "s   " + PCT((totalLoadTime / totalOrigLoadTime) - 1));
        System.out.println("Average Precalculation time v6 " + FT(totalOrigLoadTime / totalLoopCount) + "s");
        System.out.println("Average Precalculation memory v3 " + MB(totalMem / totalLoopCount) + " " + PCT((totalMem / totalOrigMem) - 1));
        System.out.println("Average Precalculation memory v6 " + MB(totalOrigMem / totalLoopCount) + " ");
        System.out.println("Average lookup time v3 " + FT(totalLookupTime / totalRepetitions) + "ns          " + PCT((totalLookupTime / totalOrigLookupTime) - 1));
        System.out.println("Average lookup time v6 " + FT(totalOrigLookupTime / totalRepetitions) + "ns");
        System.out.println("Total lookup results v3 " + totalMatches + "      " + (totalMatches - totalOrigMatches) + " differences");
        System.out.println("Total lookup results v6 " + totalOrigMatches);
    }

    static void loadDictionary(SymSpellV3 dict, String corpus) throws FileNotFoundException, IOException {
        File f = new File(corpus);
        if (!(f.exists() && !f.isDirectory())) {
            System.out.println("File not found: " + corpus);
            return;
        }
        Set<String> x = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] l1 = line.split("\\s+");
                if (l1.length > 0) {
                    String cc = ts.transform(l1[0].trim().toLowerCase());
                    if (!x.add(cc)) {
                        // System.err.println("Duplicate:" + cc);
                    } else {
                        dict.addWord(cc);
                    }
                }
            }
        }
        dict.commit();
    }

    static void loadDictionary(com.faroo.test.perf.algo.sp3orig.SymSpell dict, String corpus) throws FileNotFoundException, IOException {
        File f = new File(corpus);
        if (!(f.exists() && !f.isDirectory())) {
            System.out.println("File not found: " + corpus);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] l1 = line.split("\\s+");
                if (l1.length > 0) {
                    dict.createDictionaryEntry(ts.transform(l1[0].trim().toLowerCase()));
                }
            }
        }

    }

    static void loadDictionary(SymSpellV6 dict, String corpus) throws FileNotFoundException, IOException {
        File f = new File(corpus);
        if (!(f.exists() && !f.isDirectory())) {
            System.out.println("File not found: " + corpus);
            return;
        }
        SuggestionStage staging = new SuggestionStage(16384);

        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] l1 = line.split("\\s+");
                if (l1.length > 0) {
                    dict.createDictionaryEntry(ts.transform(l1[0].trim().toLowerCase()), 1l, staging);
                }
            }
        } finally {
            dict.commit(staging);
            staging = null;
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        warmUp();
        // gc();
        // gc();
        gc();
        benchmarkPrecalculationLookup();
        System.out.println();
    }

}
