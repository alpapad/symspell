package com.faroo.symspell;

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

/** Spelling suggestion returned from Lookup. */
public class SuggestItem implements Comparable<SuggestItem> {
	/* The suggested correctly spelled word. */
	public final String term;
	/* Edit distance between searched for word and suggestion. */
	public final int distance;
	/*
	 * Frequency of suggestion in the dictionary (a measure of how common the word
	 * is).
	 */
	public final long count;

	/**
	 * Create a new instance of SuggestItem.
	 * 
	 * @param term
	 *            The suggested word.
	 * @param distance
	 *            Edit distance from search word.
	 * @param count
	 *            Frequency of suggestion in dictionary.
	 * 
	 */
	public SuggestItem(String term, int distance, long count) {
		this.term = term;
		this.distance = distance;
		this.count = count;
	}

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