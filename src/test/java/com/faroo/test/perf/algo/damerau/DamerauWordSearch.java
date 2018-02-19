package com.faroo.test.perf.algo.damerau;

import java.util.ArrayList;
import java.util.List;

import com.faroo.test.perf.WordSearch;


public class DamerauWordSearch implements WordSearch {

	boolean commited = false;
	private final DamerauEngine engine = new DamerauEngine();
	private final int distance;

	public DamerauWordSearch(int dist) {
		this.distance = dist;
	}

	public void finishIndexing() {
	}

	@Override
	public boolean indexWord(String word) {
		return engine.createDictionaryEntry(word);
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		List<String> results = new ArrayList<>();
		engine.lookup(searchQuery.trim().toLowerCase(),  Math.min(dist(searchQuery), this.distance)).forEach(item -> results.add(item.term));
		return results;
	}


	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
