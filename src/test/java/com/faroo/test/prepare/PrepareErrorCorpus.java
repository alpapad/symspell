package com.faroo.test.prepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.faroo.symspell.distance.DamerauLevenshteinDistance;
import com.faroo.symspell.distance.JaroWinklerDistance;
import com.faroo.symspell.distance.LevenshteinDistance;
import com.faroo.symspell.distance.OptimalStringAlignmentDistance;
import com.faroo.symspell.distance.Sift4;
import com.faroo.test.perf.algo.bk.StringMatcher;
import com.faroo.test.unit.TestData;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.ibm.icu.text.Transliterator;

public class PrepareErrorCorpus {

	static String path = "/home/alpapad/git/symspell/src/test/resources/test_data/";
	static String corpus = "/test_data/frequency_dictionary_en_500_000.txt";
	
	static String errors = "/test_data/errors.txt";

	static String[] dollar = {"/test_data/aspell.dat","/test_data/MASTERSDAT.643", "/test_data/missp.dat", "/test_data/holbrook-missp.dat"};

	static StringMatcher<Void> matcher;
	
	static String id = "Any-Latin; nfd; [:nonspacing mark:] remove; nfc";
	static Transliterator ts = Transliterator.getInstance(id);
	
	static List<String> lookup(String searchQuery, int distance) {
		return matcher.search(searchQuery.trim().toLowerCase(),  distance)
				.stream().map(item -> item.getKeyword().toString())//
				.collect(Collectors.toList());
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
	    SetMultimap<String, String> trouthValues =
	            MultimapBuilder.hashKeys().hashSetValues(100).build();
	    LinkedHashSet<String> correct = new LinkedHashSet<>();
	    Set<String> found;
	    for(String file: dollar) {
	        found = parseDollarFile(file, trouthValues);
	        correct.addAll(found);
	    }
	    
	    found = parseErrorCorrect(errors, ",", trouthValues);
	    correct.addAll(found);
	    
	    found = parseErrorCorrect("/test_data/batch0.tab", "\\s+", trouthValues);
        correct.addAll(found);
        
        found = parseErrorCorrect("/test_data/noisy_query_en_1000.txt", "\\s+", trouthValues);
        correct.addAll(found);
        
	    Set<String> corpus = loadCorpus("/test_data/frequency_dictionary_en_500_000.txt");
	    corpus.addAll(loadCorpus("/test_data/frequency_dictionary_en_30_000.txt"));
	    corpus.addAll(loadCorpus("/test_data/frequency_dictionary_en_82_765.txt"));
        
	    corpus.addAll(correct);
	    OptimalStringAlignmentDistance osa = new OptimalStringAlignmentDistance();
	    LevenshteinDistance lev = new LevenshteinDistance();
	    JaroWinklerDistance jw = new JaroWinklerDistance();
	    Sift4 sift4 = new Sift4();
	    DamerauLevenshteinDistance dam = new DamerauLevenshteinDistance();
	    LetterPairSimilarity lps = new LetterPairSimilarity();
	    StringComparator eu = new StringComparator();
	    trouthValues.forEach((v, e) -> {
	        System.err.println(e + " ----> " + v + " " + osa.distance(e, v, 100) 
	            + " " + lev.distance(e, v, 100)+ " " + dam.distance(e, v, 100) + " " + jw.apply(e, v) 
	            + "  " + sift4.distance(e, v, 100) + "  " + lps.compareStrings(e, v) + " " + eu.compare(e, v) + " " + eu.fuzzyMatch(e, v) );
	    });
	    
	    System.err.println(corpus.size());
	    
	    //for()
//
//		Set<String> correct = loadCorrect();
//		System.err.println("Loaded correct words  (" + correct.size() + ")");
//		Set<String> corpus = loadCorpus();
//		System.err.println("Loaded corpus (" + corpus.size() + ")");
//
//		Set<String> query = loadQuery();
//		System.err.println("Loaded queries...");
//
//		corpus.addAll(correct);
//		dumpCorpus(path + "/../corpus_1_500k.txt", corpus);
//
//		for (int distance = 0; distance <= 3; distance++) {
//			matcher = new StringMatcher<>();
//			for (String word : corpus) {
//				matcher.add(ts.transform(word).trim().toLowerCase(), (Void)null);
//			}
//			System.err.println("Done indexing (distance =" + distance + ")...");
//			Map<String, List<String>> results = new HashMap<>();
//			for (String word : query) {
//				List<String> found = lookup(ts.transform(word), distance);
//
//				results.put(word, found);
//			}
//			System.err.println("Saving  results (distance =" + distance + ")...");
//			dumpTestFile(path + "/../test_dist_" + distance + "_500k.txt", results);
//		}
	}

	private static Set<String> parseDollarFile(String file, SetMultimap<String, String> trouthValues) throws UnsupportedEncodingException, IOException {
	    Set<String> testList = new HashSet<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(TestData.class.getResourceAsStream(file), "UTF8"))) {
            String line;
            String correct = null;
            while ((line = br.readLine()) != null) {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length >= 1) {
                    if (lineParts[0] != null && lineParts[0].trim().length() > 0) {
                        String value = lineParts[0].toLowerCase().trim().replaceAll("\"", "");
                        if(value.startsWith("$") ) {
                            correct = ts.transform(value.substring(1));
                            testList.add(correct);
                        } else  if(correct != null){
                            trouthValues.put(correct, ts.transform(value));
                        }
                    }
                }
            }
        } 
        return testList;
    }

    static Set<String> parseErrorCorrect(String file, String sepRegex, SetMultimap<String, String> trouthValues) throws UnsupportedEncodingException, IOException {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestData.class.getResourceAsStream(file), "UTF8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split(sepRegex);
				if (lineParts.length >= 2) {
					if (lineParts[0] != null && lineParts[0].trim().length() > 0 && lineParts[1] != null && lineParts[1].trim().length() > 0) {
					    String error = ts.transform(lineParts[0].trim().toLowerCase().replaceAll("\"", ""));
					    String correct =ts.transform(lineParts[1].trim().toLowerCase().replaceAll("\"", ""));
					    trouthValues.put(correct, error);
					    testList.add(correct);
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

	static Set<String> loadCorpus(String file) throws IOException {
		Set<String> testList = new HashSet<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestData.class.getResourceAsStream(file), "UTF8"))) {
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

	   static Set<String> loadMissp() throws IOException {
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
