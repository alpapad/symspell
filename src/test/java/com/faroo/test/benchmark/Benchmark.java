package com.faroo.test.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.faroo.symspell.SymSpell;
import com.faroo.symspell.Verbosity;
import com.google.common.base.Stopwatch;
import com.ibm.icu.text.Transliterator;

public class Benchmark {

	static String path = "src/test/resources/test_data";
	//static String errors = path + "/noisy_query_en_1000.txt";
	//static String errors = path + "/batch0.tab";
	static String query1k = path + "/errors.txt";
	static String[] dictionaryPath = { //
			path + "/frequency_dictionary_en_30_000.txt",//
			path + "/frequency_dictionary_en_82_765.txt",//
			path + "/frequency_dictionary_en_500_000.txt" };

	static String[] dictionaryName = { "30k", "82k", "500k" };

	static int[] dictionarySize = { 29159, 82765, 500000 };

	static String id = "Any-Latin; nfd; [:nonspacing mark:] remove; nfc";
	static Transliterator ts = Transliterator.getInstance(id);
	
	static void gc() {
		for (int i = 0; i < 2; i++) {
			System.gc();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
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
					if(lineParts[0] != null && lineParts[0].trim().length() >0) {
						testList.add(ts.transform(lineParts[0].toLowerCase().trim().replaceAll("\"", "")));
					}
				}
			}
		}
		return testList;
	}

	// pre-run to ensure code has executed once before timing benchmarks
	static void warmUp() throws FileNotFoundException, IOException {
		SymSpell dict = new SymSpell();
		loadDictionary(dict, dictionaryPath[0]);
		dict.lookup("hockie", Verbosity.All, 1);
		
		com.faroo.test.perf.algo.sp6.SymSpell dictOrig = new com.faroo.test.perf.algo.sp6.SymSpell(2);
		dictOrig.loadDictionary(dictionaryPath[0], 0, 1);

		
		dictOrig.lookup("hockie", com.faroo.test.perf.algo.sp6.SymSpell.Verbosity.All, 1);
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
		for (int maxEditDistance = 2; maxEditDistance <= 3; maxEditDistance++) {

			// benchmark dictionary precalculation size and time
			// maxEditDistance=1/2/3; prefixLength=5/6/7; dictionary=30k/82k/500k;
			// class=instantiated/static
			for (int i = 0; i < dictionaryPath.length; i++) {
				totalLoopCount++;

				// instantiated dictionary
				long memSize = getTotalMemory();
				memSize = getTotalMemory();
				stopWatch.reset().start();
				SymSpell dict = new SymSpell(maxEditDistance);
				loadDictionary(dict, dictionaryPath[i]);
				stopWatch.stop();
				long memDelta = getTotalMemory() - memSize;

				totalLoadTime += stopWatch.elapsed().getSeconds();
				totalMem += memDelta / 1024.0 / 1024.0;
				System.out.println("Precalculation v7 " + stopWatch.elapsed().getSeconds() + "s "
						+ (memDelta / 1024.0 / 1024.0) + "MB " + dict.getWordCount() + " words " + dict.getEntryCount()
						+ " entries  MaxEditDistance=" + maxEditDistance +" dict="
						+ dictionaryName[i]);

				// static dictionary
				memSize = getTotalMemory();
				stopWatch.reset().start();
				com.faroo.test.perf.algo.sp6.SymSpell dictOrig = new com.faroo.test.perf.algo.sp6.SymSpell(maxEditDistance);
				dictOrig.loadDictionary(dictionaryPath[i], 0, 1);
				//LoadDictionary(dictOrig, DictionaryPath[i]);
				totalOrigLoadTime += stopWatch.stop().elapsed().getSeconds();

				memDelta = getTotalMemory() - memSize;
				totalOrigMem += memDelta / 1024.0 / 1024.0;

				System.out.println("Precalculation v3 " + stopWatch.elapsed().getSeconds() + "s "
						+ (memDelta / 1024 / 1024.0) + "MB " + dictOrig.getWordCount() + " words "
						+ dictOrig.getEntryCount() + " entries  MaxEditDistance=" + maxEditDistance  + " dict=" + dictionaryName[i]);

				// benchmark lookup result number and time
				// maxEditDistance=1/2/3; prefixLength=5/6/7; dictionary=30k/82k/500k;
				// verbosity=0/1/2; query=exact/non-exact/mix; class=instantiated/static
				for (Verbosity verbosity : Verbosity.values()) {
					// instantiated exact
					stopWatch.reset().start();
					for (int round = 0; round < repetitions; round++) {
						resultNumber = dict.lookup("different", verbosity, maxEditDistance).size();
					}

					totalLookupTime += stopWatch.stop().elapsed().toNanos();
					totalMatches += resultNumber;
					System.out.println("lookup v7 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity
							+ " query=exact");
					// static exact
					stopWatch.reset().start();
					for (int round = 0; round < repetitions; round++) {
						resultNumber = dictOrig.lookup("different", com.faroo.test.perf.algo.sp6.SymSpell.Verbosity.valueOf(verbosity.name()), maxEditDistance).size();
					}

					totalOrigLookupTime += stopWatch.stop().elapsed().toNanos();
					totalOrigMatches += resultNumber;
					System.out.println("lookup v3 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity
							+ " query=exact");
					System.out.println();
					totalRepetitions += repetitions;

					// instantiated non-exact
					stopWatch.reset().start();
					for (int round = 0; round < repetitions; round++) {
						resultNumber = dict.lookup("hockie", verbosity, maxEditDistance).size();
					}
					stopWatch.stop();
					totalLookupTime += stopWatch.elapsed().toNanos();
					totalMatches += resultNumber;
					System.out.println("lookup v7 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity
							+ " query=non-exact");
					// static non-exact
					stopWatch.reset().start();
					for (int round = 0; round < repetitions; round++) {
						resultNumber = dictOrig.lookup("hockie", com.faroo.test.perf.algo.sp6.SymSpell.Verbosity.valueOf(verbosity.name()), maxEditDistance).size();
					}
					stopWatch.stop();
					totalOrigLookupTime += stopWatch.elapsed().toNanos();
					totalOrigMatches += resultNumber;
					System.out.println("lookup v3 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / repetitions) + "ns/op verbosity=" + verbosity
							+ " query=non-exact");
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
					System.out.println("lookup v7 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / (double) query1k.size()) + "ns/op verbosity="
							+ verbosity + " query=mix");
					// static mix
					stopWatch.reset().start();
					resultNumber = 0;
					for (String word : query1k) {
						resultNumber += dictOrig.lookup(word, com.faroo.test.perf.algo.sp6.SymSpell.Verbosity.valueOf(verbosity.name()), maxEditDistance).size();
					}

					totalOrigLookupTime += stopWatch.stop().elapsed().toNanos();
					totalOrigMatches += resultNumber;
					System.out.println("lookup v3 " + resultNumber + " results "
							+ (stopWatch.elapsed().toNanos() / (double) query1k.size()) + "ns/op verbosity="
							+ verbosity + " query=mix");
					System.out.println();
					totalRepetitions += query1k.size();
				}
				System.out.println();

				dict = null;
				dictOrig = null;
			}
		}
		System.out.println("Average Precalculation time v7 " + (totalLoadTime / totalLoopCount) + "s   "	+ ((totalLoadTime / totalOrigLoadTime) - 1));
		System.out.println("Average Precalculation time v3 " + (totalOrigLoadTime / totalLoopCount) + "s");
		System.out.println("Average Precalculation memory v7 " + (totalMem / totalLoopCount) + "MB " + ((totalMem / totalOrigMem) - 1));
		System.out.println("Average Precalculation memory v3 " + (totalOrigMem / totalLoopCount) + "MB");
		System.out.println("Average lookup time v7 " + (totalLookupTime / totalRepetitions) + "ns          " + ((totalLookupTime / totalOrigLookupTime) - 1));
		System.out.println("Average lookup time v3 " + (totalOrigLookupTime / totalRepetitions) + "ns");
		System.out.println("Total lookup results v7 " + totalMatches + "      " + (totalMatches - totalOrigMatches) + " differences");
		System.out.println("Total lookup results v3 " + totalOrigMatches);
	}

	static void loadDictionary(SymSpell dict, String corpus) throws FileNotFoundException, IOException {
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
		dict.commit();
	}

	static void loadDictionary(com.faroo.test.perf.algo.sp3.SymSpell dict, String corpus) throws FileNotFoundException, IOException {
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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		warmUp();
		//gc();
		//gc();
		gc();
		benchmarkPrecalculationLookup();
		System.out.println();
	}

}
