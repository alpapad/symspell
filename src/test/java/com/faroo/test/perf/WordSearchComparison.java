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
//import org.apache.commons.lang3.time.StopWatch;

import com.faroo.test.perf.algo.sp3orig.SymSpell3WordSearch;
import com.faroo.test.perf.algo.sp6.SymSpell6WordSearch;
import com.faroo.test.perf.algo.sp7.SymSpell7WordSearch;
import com.google.common.base.Stopwatch;
import com.google.common.math.StatsAccumulator;

//import de.cxp.predict.common.Eudex;
//import de.cxp.spellcorrect.damerau.DamerauWordSearch;
//import de.cxp.spellcorrect.elasticsearch.ElasticsearchWordSearch;
//import de.cxp.spellcorrect.fastfuzzystringmatcher.BkWordSearch;
//import de.cxp.spellcorrect.levenshtein.LevenshteinWordSearch;
//import de.cxp.spellcorrect.lucene.LuceneWordSearch;
//import de.cxp.spellcorrect.predict.PreDictFactory;
//import de.cxp.spellcorrect.symspell.v5.SymSpellWordSearch;
//import de.cxp.spellcorrect.symspell.v6.SymSpellWordSearch6;
//import de.cxp.spellcorrect.symspell.v7.SymSpellWordSearch7;

public class WordSearchComparison {

	// private static String fullTestData = "src/main/resources/full_test.txt";
	// private static String fullTestData = "src/main/resources/f1.txt";
	private static String fullTestData = "src/test/resources/cen.txt";
	// private static String testSet1 = "src/main/resources/spell-testset1.txt";
	// private static String testSet2 = "src/main/resources/spell-testset2.txt";
	// private static String testSet3 = "src/main/resources/spell-testset3.txt";
	// private static String cesnsus = "src/main/resources/Names_2010Census.csv";
	// very verbose!!
	private static boolean printFailures = false;

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
		WordSearch[] wordCorrectImplementations = { //
				// new BkWordSearch(2), //
				//new SymSpell3WordSearch(2), //
				//new SymSpell3WordSearch(2), //
				//new SymSpell3WordSearch(2), //
				new SymSpell6WordSearch(2), //
				
				//new SymSpell6WordSearch(2), //
				//new SymSpell6WordSearch(2), //
				new SymSpell7WordSearch(2), //
				
				new SymSpell6WordSearch(2), //
				new SymSpell7WordSearch(2), //
				//new SymSpell7WordSearch(2), //
				//new SymSpell7WordSearch(2), //
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
			WordSearch wordCorrect = wordCorrectImplementations[k];
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
					System.out.println(e.getKey() + " ---> " + acc.count()//
							+ " sum:" + df.format(acc.sum()) //
							+ "  mean:" + df.format(acc.mean())//
							+ ", Stdev:" + df.format(acc.sampleStandardDeviation()) //
							+ ", var:" + df.format(acc.sampleVariance()));
				}
			}

			System.out.println("Failed queries stats");
			for (Entry<Integer, StatsAccumulator> e : failed.entrySet()) {
				StatsAccumulator acc = e.getValue();
				if (acc.count() > 0) {
					try {
						System.out.println(e.getKey() + " ---> " + acc.count() + "  mean:" + df.format(acc.mean())
								+ ", Stdev:" + df.format(acc.sampleStandardDeviation()) + ", var:"
								+ df.format(acc.sampleVariance()));
					} catch (Exception exx) {

					}
				}
			}
			System.out.println();
		}
	}

	public static void run(WordSearch wordCorrect) throws IOException {
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		CSVParser parser = CSVParser.parse(new File(fullTestData), Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(':'));

		Map<String, String> tpCandidates = new HashMap<>();
		Map<String, String> fpCandidates = new HashMap<>();

		// index
		System.out.println("Indexing..");
		Stopwatch stopWatch = Stopwatch.createUnstarted();
		stopWatch.start();
		int indexCount = 0;
		Iterator<CSVRecord> csvIterator = parser.iterator();
		while (csvIterator.hasNext()) {
			// 0 = correct word
			// 1 = true if this is a desired match,
			// false if this is a false-positive match
			// 2 = comma separated list of similar word
			CSVRecord csvRecord = csvIterator.next();
			Boolean match = Boolean.valueOf(csvRecord.get(1));
			if (match) {
				appendToList(tpCandidates, csvRecord);
			} else {
				if (csvRecord.get(1).equals(csvRecord.get(0))) {
					System.out.println("WRONG: " + csvRecord.get(1) + "," + csvRecord.get(0) + ",false");
				}
				appendToList(fpCandidates, csvRecord);
			}

			wordCorrect.indexWord(csvRecord.get(0));
			indexCount++;
			if (indexCount % 1000 == 1) {
				System.out.print(".");
			}
			if (indexCount % 80000 == 1 && indexCount > 80000) {
				System.out.println("");
			}

		}
		wordCorrect.finishIndexing();
		Duration indexTime = stopWatch.stop().elapsed();
		System.out.println("Done ");
		gc();
		System.out.println("Memory used: "
				+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem) / 1024 / 1024
				+ "Mb");


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
//			int h = m.incrementAndGet();
//			if (h % 1000 == 1) {
//				System.err.print(".");
//			}
//			if (h % 80000 == 1 && h > 80000) {
//				System.err.println("");
//			}
			List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
			if (isMatch(candidate, results)) {
				return 1;
			} else {
				if (printFailures) {
					StringBuilder sb = new StringBuilder();
					sb.append(": '" + candidate.getValue() + "' not found by search for " + candidate.getKey());
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
//		int i = 0;
//		for (Entry<String, String> candidate : tpCandidates.entrySet()) {
//			List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
//			i++;
//			// first or second match count as success
//			if (isMatch(candidate, results)) {
//				success++;
//				truePositives++;
//			} else {
//				if (printFailures) {
//					System.out.println(
//							count + ": '" + candidate.getValue() + "' not found by search for " + candidate.getKey());
//					if (results.size() > 0)
//						System.out.print("   found '");
//					results.forEach((r) -> {
//						System.out.print("'" + r + "',");
//					});
//					System.out.println();
//				}
//				fail++;
//				falseNegatives++;
//			}
//			count++;
//			// if (i >= 1_000) {
//			// break;
//			// }
//		}

		res = fpCandidates.entrySet().parallelStream().map((candidate) -> {
			List<String> results = wordCorrect.findSimilarWords(candidate.getKey());
			if (isMatch(candidate, results) && !candidate.getKey().equals(results.get(0))) {
				return 1;
			} else {
				if (printFailures) {
					System.out.println("false-positive: found for: '" + candidate.getValue() + "'by search for "
							+ candidate.getKey());
					if (results.size() > 0)
						System.out.print("   found '");
					results.forEach((r) -> {
						System.out.print("'" + r + "',");
					});
					System.out.println();
				}
				return 0;
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
		System.out.println(elapsed.toMillis() + "ms => " + String.format("%1$.3f searches/ms", ((double) count / (elapsed.toMillis()))));
		System.out.println();
		System.out.println(success + " success / accuracy => " + String.format("%.2f%%", (100.0 * success / count)));
		System.out.println(truePositives + " true-positives");
		System.out.println(trueNegatives + " true-negatives (?)");
		System.out.println();
		System.out.println(fail + " fail => " + String.format("%.2f%%", (100.0 * fail / count)));
		System.out.println(falseNegatives + " false-negatives");
		System.out.println(falsePositives + " false-positives");
		System.out.println();
		if (wordCorrect instanceof Closeable) {
			((Closeable) wordCorrect).close();
		}
	}

	private static void appendToList(Map<String, String> tpCandidates, CSVRecord csvRecord) {
		String targetWord = csvRecord.get(0);
		String[] variants = csvRecord.get(2).split(",");
		for (String variant : variants) {
			tpCandidates.put(variant, targetWord);
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
			// System.err.println(candidate + ":" + results.size() + " " + results);
		}
		return found;
		//
		// return (results.size() > 0 && results.get(0).equals(candidate.getValue()))
		// || (results.size() > 0 && results.get(0).equals(candidate.getKey()))
		// || (acceptSecondHitAsSuccess
		// && results.size() > 1
		// && results.get(1).equals(candidate.getValue()));
	}

}
