package com.faroo.test.perf.algo.sp3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.faroo.symspell.distance.DamerauLevenshteinDistance;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

// SymSpell: 1 million times faster through Symmetric Delete spelling correction algorithm
//
// The Symmetric Delete spelling correction algorithm reduces the complexity of edit candidate generation and dictionary lookup
// for a given Damerau-Levenshtein distance. It is six orders of magnitude faster and language independent.
// Opposite to other algorithms only deletes are required, no transposes + replaces + inserts.
// Transposes + replaces + inserts of the input term are transformed into deletes of the dictionary term.
// Replaces and inserts are expensive and language dependent: e.g. Chinese has 70,000 Unicode Han characters!
//
// Copyright (C) 2015 Wolf Garbe
// Version: 3.0
// Author: Wolf Garbe <wolf.garbe@faroo.com>
// Maintainer: Wolf Garbe <wolf.garbe@faroo.com>
// URL: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
// Description: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
//
// License:
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License,
// version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
// http://www.opensource.org/licenses/LGPL-3.0
//
// Usage: single word + Enter:  Display spelling suggestions
//        Enter without input:  Terminate the program

public class SymSpell {
	public int editDistanceMax = 2;
	private int verbose = 2;
	// 0: top suggestion
	// 1: all suggestions of smallest edit distance
	// 2: all suggestions <= editDistanceMax (slower, no early termination)

	private static class DictionaryItem {
		public TIntList suggestions = new TIntArrayList(1);
		public int count = 0;
	}

	public static class SuggestItem implements Comparable<SuggestItem> {
		public String term = "";
		public int distance = 0;
		public long count = 0;


		@Override
		public int compareTo(SuggestItem other) {
			// order by distance ascending, then by frequency count descending
			if (this.distance == other.distance) {
				return Long.compare(other.count, this.count);
			}
			return Integer.compare(this.distance, other.distance);
		}

		@Override
		public boolean equals(Object obj) {
			return term.equals(((SuggestItem) obj).term);
		}

		@Override
		public int hashCode() {
			return term.hashCode();
		}

		@Override
		public String toString() {
			return "{" + term + ", " + distance + ", " + count + "}";
		}
	}

	// Dictionary that contains both the original words and the deletes derived from
	// them. A term might be both word and delete from another word at the same
	// time.
	// For space reduction a item might be either of type dictionaryItem or Int.
	// A dictionaryItem is used for word, word/delete, and delete with multiple
	// suggestions. Int is used for deletes with a single suggestion (the majority
	// of entries).
	private Map<String, Object> dictionary = new HashMap<String, Object>(); // initialisierung

	// List of unique words. By using the suggestions (Int) as index for this list
	// they are translated into the original String.
	private List<String> wordlist = new ArrayList<String>();

	public int maxlength = 0;// maximum dictionary term length

	public SymSpell() {
		super();
	}

	com.faroo.symspell.distance.IDistance d= new DamerauLevenshteinDistance();
	
	public SymSpell(int editDistanceMax) {
		super();
		this.editDistanceMax = editDistanceMax;
	}

	// for every word there all deletes with an edit distance of 1..editDistanceMax
	// created and added to the dictionary
	// every delete entry has a suggestions list, which points to the original
	// term(s) it was created from
	// The dictionary may be dynamically updated (word frequency and new words) at
	// any time by calling createDictionaryEntry
	public boolean createDictionaryEntry(String key) {
		boolean result = false;
		DictionaryItem value = null;
		Object valueo;
		valueo = dictionary.get(key);
		if (valueo != null) {
			// int or dictionaryItem? delete existed before word!
			if (valueo instanceof Integer) {
				int tmp = (int) valueo;
				value = new DictionaryItem();
				value.suggestions.add(tmp);
				dictionary.put(key, value);
			}

			// already exists:
			// 1. word appears several times
			// 2. word1==deletes(word2)
			else {
				value = (DictionaryItem) valueo;
			}

			// prevent overflow
			if (value.count < Integer.MAX_VALUE) {
				value.count++;
			}
		} else if (wordlist.size() < Integer.MAX_VALUE) {
			value = new DictionaryItem();
			value.count++;
			dictionary.put(key, value);

			if (key.length() > maxlength) {
				maxlength = key.length();
			}
		}

		// edits/suggestions are created only once, no matter how often word occurs
		// edits/suggestions are created only as soon as the word occurs in the corpus,
		// even if the same term existed before in the dictionary as an edit from
		// another word
		// a treshold might be specifid, when a term occurs so frequently in the corpus
		// that it is considered a valid word for spelling correction
		if (value.count == 1) {
			// word2index
			wordlist.add(key);
			int keyint = wordlist.size() - 1;

			result = true;

			// create deletes
			for (String delete : edits(key, 0, new HashSet<String>())) {
				Object value2 = dictionary.get(delete);
				if (value2 != null) {
					// already exists:
					// 1. word1==deletes(word2)
					// 2. deletes(word1)==deletes(word2)
					// int or dictionaryItem? single delete existed before!
					if (value2 instanceof Integer) {
						// transformes int to dictionaryItem
						int tmp = (int) value2;
						DictionaryItem di = new DictionaryItem();
						di.suggestions.add(tmp);
						dictionary.put(delete, di);
						if (!di.suggestions.contains(keyint)) {
							addLowestDistance(di, key, keyint, delete);
						}
					} else if (!((DictionaryItem) value2).suggestions.contains(keyint)) {
						addLowestDistance((DictionaryItem) value2, key, keyint, delete);
					}
				} else {
					dictionary.put(delete, keyint);
				}

			}
		}
		return result;
	}

	// save some time and space
	private void addLowestDistance(DictionaryItem item, String suggestion, int suggestionint, String delete) {
		// remove all existing suggestions of higher distance, if verbose<2
		// index2word
		// TODO check
		if ((verbose < 2) && (item.suggestions.size() > 0) && ((wordlist.get(item.suggestions.get(0)).length()
				- delete.length()) > (suggestion.length() - delete.length()))) {
			item.suggestions.clear();
		}
		// do not add suggestion of higher distance than existing, if verbose<2
		if ((verbose == 2) || (item.suggestions.size() == 0) || ((wordlist.get(item.suggestions.get(0)).length()
				- delete.length()) >= (suggestion.length() - delete.length()))) {
			item.suggestions.add(suggestionint);
		}
	}

	// inexpensive and language independent: only deletes, no transposes + replaces
	// + inserts
	// replaces and inserts are expensive and language dependent (Chinese has 70,000
	// Unicode Han characters)
	private Set<String> edits(String word, int editDistance, Set<String> deletes) {
		editDistance++;
		if (word.length() > 1) {
			for (int i = 0; i < word.length(); i++) {
				// delete ith character
				String delete = word.substring(0, i) + word.substring(i + 1);
				if (deletes.add(delete)) {
					// recursion, if maximum edit distance not yet reached
					if (editDistance < editDistanceMax) {
						edits(delete, editDistance, deletes);
					}
				}
			}
		}
		return deletes;
	}

	public List<SuggestItem> lookup(String input, int editDistanceMax) {
		return lookup(input,  editDistanceMax, this.verbose); 
	}
	
	public List<SuggestItem> lookup(String input, int editDistanceMax, int verbose) {
		// save some time
		if ((input.length() - editDistanceMax) > maxlength) {
			return Collections.emptyList();
		}

		List<String> candidates = new ArrayList<String>();
		Set<String> candidatesUniq = new HashSet<String>();

		List<SuggestItem> suggestions = new ArrayList<SuggestItem>();
		Set<String> checkedWords = new HashSet<String>();

		Object dictionaryEntry;

		// add original term
		candidates.add(input);

		while (candidates.size() > 0) {
			String candidate = candidates.get(0);
			candidates.remove(0);

			// save some time
			// early termination
			// suggestion distance=candidate.distance... candidate.distance+editDistanceMax
			// if canddate distance is already higher than suggestion distance, than there
			// are no better suggestions to be expected

			// label for c# goto replacement
			nosort: {

				if ((verbose < 2) && (suggestions.size() > 0) && ((input.length() - candidate.length()) > suggestions.get(0).distance)) {
					break nosort;
				}

				// read candidate entry from dictionary
				dictionaryEntry = dictionary.get(candidate);
				if (dictionaryEntry != null) {
					DictionaryItem matchedDictionaryItem;
					if (dictionaryEntry instanceof Integer) {
						matchedDictionaryItem = new DictionaryItem();
						matchedDictionaryItem.suggestions.add(Integer.class.cast(dictionaryEntry).intValue());
					} else {
						matchedDictionaryItem = DictionaryItem.class.cast(dictionaryEntry);
					}

					// if count>0 then candidate entry is correct dictionary term, not only delete
					// item
					if ((matchedDictionaryItem.count > 0) && checkedWords.add(candidate)) {
						// add correct dictionary term term to suggestion list
						SuggestItem si = new SuggestItem();
						si.term = candidate;
						si.count = matchedDictionaryItem.count;
						si.distance = input.length() - candidate.length();
						suggestions.add(si);
						// early termination
						if ((verbose < 2) && ((input.length() - candidate.length()) == 0)) {
							break nosort;
						}
					}

					// iterate through suggestions (to other correct dictionary items) of delete
					// item and add them to suggestion list
					//Object value2;
					for (int suggestionint : matchedDictionaryItem.suggestions.toArray()) {
						// save some time
						// skipping double items early: different deletes of the input term can lead to
						// the same suggestion
						// index2word
						// TODO
						String suggestion = wordlist.get(suggestionint);
						if (checkedWords.add(suggestion)) {
							// True Damerau-Levenshtein Edit Distance: adjust distance, if both distances>0
							// We allow simultaneous edits (deletes) of editDistanceMax on on both the
							// dictionary and the input term.
							// For replaces and adjacent transposes the resulting edit distance stays <=
							// editDistanceMax.
							// For inserts and deletes the resulting edit distance might exceed
							// editDistanceMax.
							// To prevent suggestions of a higher edit distance, we need to calculate the
							// resulting edit distance, if there are simultaneous edits on both sides.
							// Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and
							// bank!=baxn for editDistanceMaxe=1)
							// Two deletes on each side of a pair makes them all equal, but the first two
							// pairs have edit distance=1, the others edit distance=2.
							int distance = 0;
							if (!suggestion.equals(input)) {
								if (suggestion.length() == candidate.length()) {
									distance = input.length() - candidate.length();
								} else if (input.length() == candidate.length()) {
									distance = suggestion.length() - candidate.length();
								} else {
									// common prefixes and suffixes are ignored, because this speeds up the
									// Damerau-levenshtein-Distance calculation without changing it.
									int ii = 0;
									int jj = 0;
									while ((ii < suggestion.length()) && (ii < input.length())
											&& (suggestion.charAt(ii) == input.charAt(ii))) {
										ii++;
									}
									while ((jj < (suggestion.length() - ii)) && (jj < (input.length() - ii))
											&& (suggestion.charAt(suggestion.length() - jj - 1) == input
													.charAt(input.length() - jj - 1))) {
										jj++;
									}
									if ((ii > 0) || (jj > 0)) {
										distance = damerauLevenshteinDistance(
												suggestion.substring(ii, suggestion.length() - jj),
												input.substring(ii, input.length() - jj));
									} else {
										distance = damerauLevenshteinDistance(suggestion, input);
									}
								}
							}

							// save some time.
							// remove all existing suggestions of higher distance, if verbose<2
							if ((verbose < 2) && (suggestions.size() > 0) && (suggestions.get(0).distance > distance)) {
								suggestions.clear();
							}
							// do not process higher distances than those already found, if verbose<2
							if ((verbose < 2) && (suggestions.size() > 0) && (distance > suggestions.get(0).distance)) {
								continue;
							}

							if (distance <= editDistanceMax) {
								Object value2 = dictionary.get(suggestion);
								if (value2 != null) {
									SuggestItem si = new SuggestItem();
									si.term = suggestion;
									si.count = ((DictionaryItem) value2).count;
									si.distance = distance;
									suggestions.add(si);
								}
							}
						}
					} // end foreach
				} // end if

				// add edits
				// derive edits (deletes) from candidate (input) and add them to candidates list
				// this is a recursive process until the maximum edit distance has been reached
				if ((input.length() - candidate.length()) < editDistanceMax) {
					// save some time
					// do not create edits with edit distance smaller than suggestions already found
					if ((verbose < 2) && (suggestions.size() > 0)
							&& ((input.length() - candidate.length()) >= suggestions.get(0).distance)) {
						continue;
					}

					for (int i = 0; i < candidate.length(); i++) {
						String delete = candidate.substring(0, i) + candidate.substring(i + 1);
						if (candidatesUniq.add(delete)) {
							candidates.add(delete);
						}
					}
				}
			} // end lable nosort
		} // end while
		
		Collections.sort(suggestions,(x, y) -> x.compareTo(y));
		if ((verbose == 0) && (suggestions.size() > 1)) {
			return suggestions.subList(0, 1);
		} else {
			return suggestions;
		}
	}

	public int damerauLevenshteinDistance(String a, String b) {
		return d.distance(a, b, editDistanceMax);
	}
	/**
	 * Damerau–Levenshtein distance algorithm and code from
	 * http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance (as
	 * retrieved in June 2012)
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int xdamerauLevenshteinDistance(String a, String b) {
		final int inf = a.length() + b.length() + 1;
		int[][] H = new int[a.length() + 2][b.length() + 2];
		for (int i = 0; i <= a.length(); i++) {
			H[i + 1][1] = i;
			H[i + 1][0] = inf;
		}
		for (int j = 0; j <= b.length(); j++) {
			H[1][j + 1] = j;
			H[0][j + 1] = inf;
		}
		HashMap<Character, Integer> DA = new HashMap<Character, Integer>();
		for (int d = 0; d < a.length(); d++) {
			if (!DA.containsKey(a.charAt(d))) {
				DA.put(a.charAt(d), 0);
			}
		}

		for (int d = 0; d < b.length(); d++) {
			if (!DA.containsKey(b.charAt(d))) {
				DA.put(b.charAt(d), 0);
			}
		}

		for (int i = 1; i <= a.length(); i++) {
			int DB = 0;
			for (int j = 1; j <= b.length(); j++) {
				final int i1 = DA.get(b.charAt(j - 1));
				final int j1 = DB;
				int d = 1;
				if (a.charAt(i - 1) == b.charAt(j - 1)) {
					d = 0;
					DB = j;
				}
				H[i + 1][j + 1] = min(H[i][j] + d, H[i + 1][j] + 1, H[i][j + 1] + 1,
						H[i1][j1] + ((i - i1 - 1)) + 1 + ((j - j1 - 1)));
			}
			DA.put(a.charAt(i - 1), i);
		}
		return H[a.length() + 1][b.length() + 1];
	}

	public static int min(int a, int b, int c, int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}

	public int getWordCount() {
		return wordlist.size();
	}

	public int getEntryCount() {
		return dictionary.size();
	}
}
