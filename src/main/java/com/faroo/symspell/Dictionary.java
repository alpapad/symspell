/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell;

//import static com.faroo.symspell.SipHash.hash;
import static com.faroo.symspell.hash.XxHash64.hash;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

/**
 * Dictionary that contains both the original words and the deletes derived from
 * them. A term might be both word and delete from another word at the same
 * time.
 */
public class Dictionary {
	private TLongObjectMap<Object> tempDictionary = new TLongObjectHashMap<>();

	private List<String> tempWordlist = new ArrayList<String>();

	/**
	 * Dictionary that contains both the original words and the deletes derived from
	 * them. A term might be both word and delete from another word at the same
	 * time.
	 * 
	 * Instead of keeping the words as pointers, we use a very low collision hash of
	 * the word as a key.
	 * 
	 * For space reduction a item might be either of type of the values can be
	 * either an integer or a DictionaryItem
	 * 
	 * A DictionaryItem is used for word, word/delete, and delete with multiple
	 * suggestions. integer is used for deletes with a single suggestion (the
	 * majority of entries).
	 */
	private TLongObjectMap<Object> dictionary = new TLongObjectHashMap<>();

	/**
	 * List of unique words. By using the suggestions (Int) as index for this list
	 * they are translated into the original String.
	 */
	private String[] words;
	/**
	 * array of pre-calculated hashes to speedup lookup of index words (no need to
	 * calculate the hash again)
	 */
	private long[] hashes;

	private int verbose = 2;
	private int restricetdEditDistanceMax = 2;

	public int maxlength = 0;// maximum tempDictionary term length

	private static class Node {
		public List<Integer> suggestions = new ArrayList<>();
		public int count = 0;
	}

	public Dictionary(int editDistanceMax, Verbosity verbosity) {
		super();
		this.restricetdEditDistanceMax = editDistanceMax;
		this.verbose = verbosity.verbose;
	}

	static int dist(int l, double k) {
		return (int) Math.round((1d - k) * l);
	}

	/**
	 * For every word there all deletes with an edit distance of 1..editDistanceMax
	 * created and added to the tempDictionary every delete entry has a suggestions
	 * list, which points to the original term(s) it was created from
	 * 
	 * The tempDictionary may be dynamically updated (word frequency and new words)
	 * at any time by calling createDictionaryEntry
	 * 
	 * @param key
	 * @return
	 */
	public boolean createDictionaryEntry(String key) {
		boolean result = false;
		Node value = null;
		Object valueo = tempDictionary.get(hash(key));
		if (valueo != null) {
			// int or dictionaryItem? delete existed before word!
			if (valueo instanceof Integer) {
				int tmp = Integer.class.cast(valueo);
				value = new Node();
				value.suggestions.add(tmp);
				tempDictionary.put(hash(key), value);
			}

			// already exists:
			// 1. word appears several times
			// 2. word1==deletes(word2)
			else {
				value = (Node) valueo;
			}

			// prevent overflow
			if (value.count < Integer.MAX_VALUE) {
				value.count++;
			}
		} else if (tempWordlist.size() < Integer.MAX_VALUE) {
			value = new Node();
			value.count++;
			tempDictionary.put(hash(key), value);

			if (key.length() > maxlength) {
				maxlength = key.length();
			}
		}

		/*
		 * edits/suggestions are created only once, no matter how often word occurs
		 * 
		 * edits/suggestions are created only as soon as the word occurs in the corpus,
		 * 
		 * even if the same term existed before in the tempDictionary as an edit from //
		 * another word
		 * 
		 * a treshold might be specifid, when a term occurs so frequently in the corpus
		 * that it is considered a valid word for spelling correction 
		 */
		if (value.count == 1) {
			// word2index
			tempWordlist.add(key);
			int keyint = tempWordlist.size() - 1;

			result = true;
			final int maxEditDist = this.restricetdEditDistanceMax;
			// create deletes
			for (String delete : edits(key, 0, new HashSet<String>(), maxEditDist)) {
				Object value2 = tempDictionary.get(hash(delete));
				if (value2 != null) {
					// already exists:
					// 1. word1==deletes(word2)
					// 2. deletes(word1)==deletes(word2)
					// int or dictionaryItem? single delete existed before!
					if (value2 instanceof Integer) {
						// transformes int to dictionaryItem
						int tmp = Integer.class.cast(value2);
						Node di = new Node();
						di.suggestions.add(tmp);
						tempDictionary.put(hash(delete), di);
						if (!di.suggestions.contains(keyint)) {
							addLowestDistance(di, key, keyint, delete);
						}
					} else if (!Node.class.cast(value2).suggestions.contains(keyint)) {
						addLowestDistance(Node.class.cast(value2), key, keyint, delete);
					}
				} else {
					tempDictionary.put(hash(delete), keyint);
				}

			}
		}
		return result;
	}

	// save some time and space
	private void addLowestDistance(Node item, String suggestion, int suggestionint, String delete) {
		// remove all existing suggestions of higher distance, if verbose<2 index2word
		// TODO check
		if ((verbose < 2) && (item.suggestions.size() > 0) && ((tempWordlist.get(item.suggestions.get(0)).length()
				- delete.length()) > (suggestion.length() - delete.length()))) {
			item.suggestions.clear();
		}
		// do not add suggestion of higher distance than existing, if verbose<2
		if ((verbose == 2) || (item.suggestions.size() == 0) || ((tempWordlist.get(item.suggestions.get(0)).length()
				- delete.length()) >= (suggestion.length() - delete.length()))) {
			item.suggestions.add(suggestionint);
		}
	}

	/**
	 * Inexpensive and language independent: only deletes, no transposes + replaces
	 * +inserts
	 * 
	 * Replaces and inserts are expensive and language dependent (Chinese has 70,000
	 * Unicode Han characters)
	 * 
	 * @param word
	 * @param editDistance
	 * @param deletes
	 * @param editDistanceMax
	 * @return
	 */

	private Set<String> edits(String word, int editDistance, Set<String> deletes, int editDistanceMax) {
		editDistance++;
		if (word.length() > 1) {
			for (int i = 0; i < word.length(); i++) {
				// delete ith character
				String delete = word.substring(0, i) + word.substring(i + 1);
				if (deletes.add(delete)) {
					// recursion, if maximum edit distance not yet reached
					if (editDistance < editDistanceMax) {
						edits(delete, editDistance, deletes, editDistanceMax);
					}
				}
			}
		}
		return deletes;
	}

	public int getMaxlength() {
		return maxlength;
	}

	public String getSuggestion(int idx) {
		return this.words[idx];
	}

	public DictionaryItem getEntry(String candidate) {
		// read candidate entry from tempDictionary
		Object dictionaryEntry = this.dictionary.get(hash(candidate));

		if (dictionaryEntry != null) {
			if (dictionaryEntry instanceof Integer) {
				DictionaryItem matchedDictionaryItem = new DictionaryItem();
				matchedDictionaryItem.suggestions = new int[] { Integer.class.cast(dictionaryEntry).intValue() };
				return matchedDictionaryItem;
			} else {
				return DictionaryItem.class.cast(dictionaryEntry);
			}
		}
		return null;
	}

	public DictionaryItem getEntry(int idx) {
		// read candidate entry from tempDictionary
		Object dictionaryEntry = this.dictionary.get(this.hashes[idx]);
		if (dictionaryEntry != null) {
			if (dictionaryEntry instanceof Integer) {
				DictionaryItem matchedDictionaryItem = new DictionaryItem();
				matchedDictionaryItem.suggestions = new int[] { Integer.class.cast(dictionaryEntry).intValue() };
				return matchedDictionaryItem;
			} else {
				return DictionaryItem.class.cast(dictionaryEntry);
			}
		}
		return null;
	}

	public void commit() {
		this.words = tempWordlist.toArray(new String[tempWordlist.size()]);
		this.tempWordlist = null;

		this.hashes = new long[words.length];

		for (int idx = 0; idx < words.length; idx++) {
			this.hashes[idx] = hash(words[idx]);
		}

		this.tempDictionary.forEachEntry(new TLongObjectProcedure<Object>() {
			@Override
			public boolean execute(long key, Object value) {
				if (value instanceof Integer) {
					dictionary.put(key, value);
				} else {
					Node itm = Node.class.cast(value);
					DictionaryItem i = new DictionaryItem();
					i.count = itm.count;
					i.suggestions = itm.suggestions.stream().mapToInt(k -> k).toArray();
					dictionary.put(key, i);
				}
				return true;
			}

		});

		tempDictionary = null;
	}

	public int getWordCount() {
		return words.length;
	}

	public int getEntryCount() {
		return dictionary.size();
	}
}
