package com.faroo.test.unit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestData {

	private final static String BASE_PATH = "C:\\WORK\\git\\symspell\\src\\test\\resources\\";

	public List<String> loadQuery() throws FileNotFoundException, IOException {
		List<String> testList = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(BASE_PATH + "corpus_500k.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line != null && line.trim().length() > 0) {
					testList.add(line.toLowerCase().trim());
				}
			}
		}
		return testList;
	}

	public Map<String, Set<String>> loadTests(int distance) throws FileNotFoundException, IOException {
		Map<String, Set<String>> tests = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(BASE_PATH + "test_dist" + distance + "_500k.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] test = line.split("\\s*==\\s*");
				String word = test[0].trim();
				Set<String> m = new HashSet<>();
				tests.put(word, m);
				if (test.length > 1) {
					String[] matches = test[1].trim().split("@@");
					for (String s : matches) {
						m.add(s);
					}
				}
			}
		} 
		return tests;
	}
}
