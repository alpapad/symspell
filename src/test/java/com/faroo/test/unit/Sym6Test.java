package com.faroo.test.unit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.faroo.symspell.SuggestItem;
import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v6.SuggestionStage;
import com.faroo.symspell.impl.v6.SymSpellV6;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class Sym6Test {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stubsuria
		TestData data = new TestData();
		for (int i = 0; i <= 3; i++) {
			System.err.println("Indexing for distance " + i);
			SymSpellV6 sp = new SymSpellV6(i);//, DistanceAlgo.Levenshtein);
			SuggestionStage stage = new SuggestionStage(16000);
			for (String w : data.loadCorpus()) {
				sp.createDictionaryEntry(w.toLowerCase(), 1l, stage);
			}
			sp.commit(stage);
			stage.clear();
			stage = null;

			Map<String, Set<String>> tests = data.loadTests(i);
			System.err.println("Searching for distance " + i + " using " + sp.getEntryCount() + " entries...");
			for (Entry<String, Set<String>> entry : tests.entrySet()) {
				final String word = entry.getKey();
				Set<String> expected = entry.getValue();

				List<SuggestItem> items = sp.lookup(word, Verbosity.All, i);
				Set<String> results = items.stream().map(itm -> itm.term).collect(Collectors.toSet());

				SetView<String> view1 = Sets.difference(expected, results);
				SetView<String> view2 = Sets.difference(results, expected);

				if (view1.size() > 0 || view2.size() > 0) {
					System.err.println("Distance " + i + " Word: '" + word + "' diff: expected#:" + expected.size()
							+ ", results#:" + results.size() + "\n\t\t" + view1 + "\n\t\t" + view2);
				}
			}
		}
	}

}
