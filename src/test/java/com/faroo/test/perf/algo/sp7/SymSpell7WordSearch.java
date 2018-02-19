package com.faroo.test.perf.algo.sp7;

import java.util.List;
import java.util.stream.Collectors;

import com.faroo.symspell.SymSpell;
import com.faroo.test.perf.WordSearch;


public class SymSpell7WordSearch implements WordSearch {

	boolean commited = false;
	private final SymSpell symSpell;
	private final int distance;

	public SymSpell7WordSearch(int distance) {
		this.distance = distance;
		this.symSpell = new SymSpell(this.distance);
	}

	public void finishIndexing() {
		this.symSpell.commit();
	}

	@Override
	public boolean indexWord(String word) {
		return symSpell.createDictionaryEntry(word.trim().toLowerCase());
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		return symSpell.lookup(searchQuery.trim().toLowerCase(), Math.min(dist(searchQuery), this.distance))//
				.stream()//
				.map(item -> item.term).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
