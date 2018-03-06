package com.faroo.test.perf.algo.sp7;

import java.util.List;
import java.util.stream.Collectors;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.distance.DistanceAlgo;
import com.faroo.symspell.impl.v3.SymSpellV3;
import com.faroo.test.perf.WordSearch;


public class SymSpell7WordSearch implements WordSearch {

	boolean commited = false;
	private final SymSpellV3 symSpell;
	private final int distance;

	public SymSpell7WordSearch(int distance) {
		this.distance = distance;
		this.symSpell = new SymSpellV3(this.distance, Verbosity.All ,DistanceAlgo.OptimalStringAlignment);
	}

	public void finishIndexing() {
		this.symSpell.commit();
	}

	@Override
	public boolean indexWord(String word) {
		return symSpell.addWord(word.trim().toLowerCase());
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
