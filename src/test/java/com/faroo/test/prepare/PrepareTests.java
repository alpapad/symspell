package com.faroo.test.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.faroo.test.perf.algo.bk.BkWordSearch;

public class PrepareTests {

	static String path = "/home/alpapad/git/symspell/src/test/resources/test_data";
	static String errors = path + "/errors.txt";
	static String corpus = path + "/frequency_dictionary_en_82_765.txt";

	public static void main(String[] args) throws IOException, InterruptedException {
		Set<String> correct = loadCorrect();
		System.err.println("Loaded correct words  (" + correct.size() + ")");
		Set<String> corpus = loadCorpus();
		System.err.println("Loaded corpus (" + corpus.size() + ")");

		Set<String> query = loadQuery();
		System.err.println("Loaded queries...");

		corpus.addAll(correct);
		dumpCorpus(path + "/../corpus.txt", corpus);

		for (int i = 0; i <= 3; i++) {
			BkWordSearch bk = new BkWordSearch(i);
			for (String word : corpus) {
				bk.indexWord(word);
			}
			bk.finishIndexing();
			System.err.println("Done indexing (dist =" + i + ")...");
			Map<String, List<String>> results = new HashMap<>();
			for (String word : query) {
				List<String> found = bk.findAllWords(word);

				results.put(word, found);
				//System.err.println(word + "==" + found);
			}
			System.err.println("Saving  results (dist =" + i + ")...");
			dumpTestFile(path + "/../test_dist" + i + ".txt", results);
		}
	}

	static Set<String> loadQuery() {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(errors))) {
			String line;
			// process a single line at a time only for memory efficiency
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(",");
				if (lineParts.length >= 2) {
					if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
						testList.add(lineParts[0].toLowerCase().trim().replaceAll("\"", ""));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testList;
	}

	static Set<String> loadCorrect() {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(errors))) {
			String line;
			// process a single line at a time only for memory efficiency
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(",");
				if (lineParts.length >= 2) {
					if (lineParts[1] != null && lineParts[1].trim().length() > 0) {
						testList.add(lineParts[1].toLowerCase().trim().replaceAll("\"", ""));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testList;
	}

	static Set<String> loadCorpus() {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
			String line;
			// process a single line at a time only for memory efficiency
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\s+");
				if (lineParts.length > 0) {
					if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
						testList.add(lineParts[0].toLowerCase().trim());
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testList;
	}

	static void dumpCorpus(String fileName, Set<String> data) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));) {
			for (String s : data) {
				writer.write(s + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void dumpTestFile(String fileName, Map<String, List<String>> data) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));) {
			for (Entry<String, List<String>> e : data.entrySet()) {
				writer.write(e.getKey());
				writer.write("==");
				writer.write(e.getValue().stream().collect(Collectors.joining("@@")));
				writer.write("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
