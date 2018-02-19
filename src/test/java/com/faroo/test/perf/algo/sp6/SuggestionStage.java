package com.faroo.test.perf.algo.sp6;
/**
 * Copyright (C) 2017 Wolf Garbe 
 * 
 * Version: 6.0
 * Author: Wolf Garbe <wolf.garbe@faroo.com>
 * Maintainer: Wolf Garbe <wolf.garbe@faroo.com>
 * URL: https://github.com/wolfgarbe/symspell
 * 
 * Description: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/ 
 * 
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, 
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An intentionally opacque class used to temporarily stage dictionary data
 * during the adding of many words. By staging the data during the building of
 * the dictionary data, significant savings of time can be achieved, as well as
 * a reduction in final memory usage.
 * 
 */
public class SuggestionStage {
	private static class Node {
		public String suggestion;
		public int next;

		public Node(String suggestion, int next) {
			super();
			this.suggestion = suggestion;
			this.next = next;
		}

	}

	private static class Entry {
		public int count;
		public int first;

		public Entry(int count, int first) {
			super();
			this.count = count;
			this.first = first;
		}

	}

	private HashMap<Long, Entry> deletes;// { get; set; }
	private ChunkArray<Node> nodes;// { get; set; }

	/**
	 * Create a new instance of SuggestionStage.
	 * 
	 * 
	 * Specifying an accurate initialCapacity is not essential, but it can help
	 * speed up processing by aleviating the need for data restructuring as the size
	 * grows.
	 * 
	 * 
	 * @param initialCapacity
	 *            The expected number of words that will be added.
	 */

	public SuggestionStage(int initialCapacity) {
		this.deletes = new HashMap<Long, SuggestionStage.Entry>(initialCapacity);
		this.nodes = new ChunkArray<Node>(initialCapacity * 2);
	}

	/** <summary>Gets the count of unique delete words. */
	public int getDeleteCount() {
		return deletes.size();
	}

	/** Gets the total count of all suggestions for all deletes. */
	public int getNodeCount() {
		return nodes.count;
	}

	/** Clears all the data from the SuggestionStaging. */
	public void clear() {
		deletes.clear();
		nodes.clear();
	}

	/**
	 * 
	 * @param deleteHash
	 * @param suggestion
	 */
	void add(long deleteHash, String suggestion) {
		Entry entry = deletes.get(deleteHash);
		if (entry == null) {
			entry = new Entry(0, -1);
		}
		int next = entry.first;
		entry.count++;
		entry.first = nodes.count;
		deletes.put(deleteHash, entry);
		nodes.add(new Node(suggestion, next));
	}

	/**
	 * 
	 * @param permanentDeletes
	 */
	void commitTo(Map<Long, String[]> permanentDeletes) {
		for (Map.Entry<Long, Entry> keyPair : deletes.entrySet()) {
			int i;
			String[] suggestions = permanentDeletes.get(keyPair.getKey());
			if (suggestions != null) {
				i = suggestions.length;
				String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + keyPair.getValue().count);
				// String[] newSuggestions = new String[suggestions.length + keyPair.getValue().count];
				// Array.Copy(suggestions, newSuggestions, suggestions.length);
				permanentDeletes.put(keyPair.getKey(), newSuggestions);
				suggestions = newSuggestions;
				// permanentDeletes[keyPair.Key] = suggestions = newSuggestions;
			} else {
				i = 0;
				suggestions = new String[keyPair.getValue().count];
				permanentDeletes.put(keyPair.getKey(), suggestions);
			}
			int next = keyPair.getValue().first;
			while (next >= 0) {
				SuggestionStage.Node node = nodes.get(next);
				suggestions[i] = node.suggestion;
				next = node.next;
				i++;
			}
		}
	}
}