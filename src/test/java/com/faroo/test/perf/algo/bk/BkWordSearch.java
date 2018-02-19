package com.faroo.test.perf.algo.bk;

import java.util.List;
import java.util.stream.Collectors;

import com.faroo.test.perf.WordSearch;



public class BkWordSearch implements WordSearch {


	boolean commited = false;
	private final StringMatcher<Void> matcher = new StringMatcher<>();

	private final int distance;

	
	public BkWordSearch(int distance) {
		this.distance = distance;
	}

	public void finishIndexing() {
	}

	@Override
	public boolean indexWord(String word) {
		matcher.add(word.trim().toLowerCase(), (Void)null);
		return true;
	}

	@Override
	
	public List<String> findSimilarWords(String searchQuery) {
		return matcher.search(searchQuery.trim().toLowerCase(),  Math.min(dist(searchQuery), this.distance))
				.stream().map(item -> item.getKeyword().toString())//
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
