package com.faroo.test.perf.algo.sp6;

import java.util.List;
import java.util.stream.Collectors;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v6.SuggestionStage;
import com.faroo.symspell.impl.v6.SymSpellV6;
import com.faroo.test.perf.WordSearch;



public class SymSpell6WordSearch implements WordSearch {

	SuggestionStage staging = new SuggestionStage(16384);

	boolean commited = false;
	private final SymSpellV6 symSpell;// = new SymSpellV3(2);
	private final int distance;

	public SymSpell6WordSearch(int distance) {
		this.distance = distance;
		this.symSpell = new SymSpellV6(this.distance);
	}

	public void finishIndexing() {
		symSpell.commit(staging);
		staging = null;
	}

	@Override
	public boolean indexWord(String word) {
		return symSpell.createDictionaryEntry(word.trim().toLowerCase(), 10l, staging);
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		return symSpell
				.lookup(searchQuery.trim().toLowerCase(), Verbosity.All,
						Math.min(dist(searchQuery), this.distance))//
				.stream()//
				.map(item -> item.term).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
