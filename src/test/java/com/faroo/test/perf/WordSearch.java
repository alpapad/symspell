package com.faroo.test.perf;

import java.util.List;

/**
 * common api for a word search & correction implementation.
 * 
 * @author Rudolf Batt
 */
public interface WordSearch {

	boolean indexWord(String word);
	
	default void finishIndexing() {
		
	}
	List<String> findSimilarWords(String searchQuery);

	default List<String> findAllWords(String searchQuery) {
		return this.findSimilarWords(searchQuery);
	}
	static int dist(int l, double k) {
		return (int)Math.round((1d-k) *l);
	}
	
	default int dist(String in) {
		return dist(in.length(), 0.75);
	}
	
	default int dist1(String in) {
		switch (in.trim().length()) {
		case 0: return 0;
		case 1: return 0;
		case 2: return 0;
		case 3: return 1;
		case 4: return 1;
		case 5: return 1;
		case 6: return 1;
		case 7: return 1;
		case 8: return 2;
		case 9: return 2;
		case 10: return 2;
		case 11: return 2;
		case 12: return 2;
		case 13: return 3;
		case 14: return 3;
		case 15: return 3;
		case 16: return 3;
		case 17: return 3;
		case 18: return 4;
		default: return 4;
		/*case 0:
		case 1:
			return 0;
		case 2:
			return 0;
		case 3:
			return 1;
		case 4:
			return 2;
*/
		}
		//return 3;
	}
}
