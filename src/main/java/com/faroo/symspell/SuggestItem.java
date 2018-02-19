/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell;


public class SuggestItem implements Comparable<SuggestItem> {
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