package com.faroo.test.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.faroo.test.perf.algo.bk.StringMatcher;
import com.faroo.test.unit.TestData;
import com.ibm.icu.text.Transliterator;

public class PrepareTests {

	static String path = "/home/alpapad/git/symspell/src/test/resources/test_data/";
	static String errors = "/test_data/errors.txt";
	static String corpus = "/test_data/frequency_dictionary_en_500_000.txt";

	static StringMatcher<Void> matcher;
	
	static String id = "Any-Latin; nfd; [:nonspacing mark:] remove; nfc";
	static Transliterator ts = Transliterator.getInstance(id);
	
	static List<String> lookup(String searchQuery, int distance) {
		return matcher.search(searchQuery.trim().toLowerCase(),  distance)
				.stream().map(item -> item.getKeyword().toString())//
				.collect(Collectors.toList());
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Set<String> correct = loadCorrect();
		System.err.println("Loaded correct words  (" + correct.size() + ")");
		Set<String> corpus = loadCorpus();
		System.err.println("Loaded corpus (" + corpus.size() + ")");

		Set<String> query = loadQuery();
		System.err.println("Loaded queries...");

		corpus.addAll(correct);
		dumpCorpus(path + "/../corpus_1_500k.txt", corpus);

		for (int distance = 0; distance <= 70; distance+=10) {
			matcher = new StringMatcher<>();
			for (String word : corpus) {
				matcher.add(ts.transform(word).trim().toLowerCase(), (Void)null);
			}
			System.err.println("Done indexing (distance =" + distance + ")...");
			Map<String, List<String>> results = new HashMap<>();
			for (String word : query) {
				List<String> found = lookup(ts.transform(word), distance);

				results.put(word, found);
			}
			System.err.println("Saving  results (distance =" + distance/10 + ")...");
			dumpTestFile(path + "/../test_dist_" + distance/10 + "_500k.txt", results);
		}
	}

	static Set<String> loadQuery() throws UnsupportedEncodingException, IOException {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestData.class.getResourceAsStream(errors), "UTF8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(",");
				if (lineParts.length >= 2) {
					if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
						testList.add(lineParts[0].toLowerCase().trim().replaceAll("\"", ""));
					}
				}
			}
		} 
		return testList;
	}

	static Set<String> loadCorrect() throws UnsupportedEncodingException, IOException {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestData.class.getResourceAsStream(errors), "UTF8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(",");
				if (lineParts.length >= 2) {
					if (lineParts[1] != null && lineParts[1].trim().length() > 0) {
						testList.add(lineParts[1].toLowerCase().trim().replaceAll("\"", ""));
					}
				}
			}
		}
		return testList;
	}

	static Set<String> loadCorpus() throws IOException {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestData.class.getResourceAsStream(corpus), "UTF8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\s+");
				if (lineParts.length > 0) {
					if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
						testList.add(lineParts[0].toLowerCase().trim());
					}
				}
			}
		}
		return testList;
	}

	static void dumpCorpus(String fileName, Set<String> data) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));) {
			for (String s : data) {
				writer.write(ts.transform(s) + "\n");
			}
		} 
	}

	static void dumpTestFile(String fileName, Map<String, List<String>> data) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));) {
			for (Entry<String, List<String>> e : data.entrySet()) {
				writer.write(e.getKey());
				writer.write("==");
				writer.write(e.getValue().stream().map(s->ts.transform(s)).collect(Collectors.joining("@@")));
				writer.write("\n");
			}
		}
	}
}
