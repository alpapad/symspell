package com.faroo.test.perf;

import java.util.List;

/**
 * common api for a word search & correction implementation.
 * 
 * @author Rudolf Batt
 */
public interface WordSearch2 {

	boolean loadIndex(String index);
	

	List<String> findSimilarWords(String searchQuery);

	default List<String> findAllWords(String searchQuery) {
		return this.findSimilarWords(searchQuery);
	}
}
