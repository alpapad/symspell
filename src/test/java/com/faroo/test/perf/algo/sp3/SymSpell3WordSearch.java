package com.faroo.test.perf.algo.sp3;

import java.util.List;
import java.util.stream.Collectors;

import com.faroo.test.perf.WordSearch;


public class SymSpell3WordSearch implements WordSearch {

	boolean commited = false;
	private final SymSpell symSpell;
	private final int distance;

	public SymSpell3WordSearch(int distance) {
		this.distance = distance;
		symSpell = new SymSpell(this.distance);
	}

	public void finishIndexing() {
	}

	@Override
	public boolean indexWord(String word) {
		return symSpell.createDictionaryEntry(word.trim().toLowerCase());
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		return symSpell.lookup(searchQuery.trim().toLowerCase(), Math.min(dist(searchQuery), this.distance))
				.stream().map(item -> item.term)//
				.collect(Collectors.toList());
	}


	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
