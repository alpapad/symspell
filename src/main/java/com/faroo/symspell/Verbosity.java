/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell;

/**
 * Controls the closeness/quantity of returned spelling suggestions.
 */
public enum Verbosity {
	/**
	 * Top suggestion with the highest term frequency of the suggestions of smallest
	 * edit distance found.
	 */
	Top(0),
	/**
	 * All suggestions of smallest edit distance found, suggestions ordered by term
	 * frequency.
	 */
	Closest(1),
	/**
	 * All suggestions within maxEditDistance, suggestions ordered by edit distance,
	 * then by term frequency (slower, no early termination).
	 */
	All(2);

	public final int verbose;

	private Verbosity(int verbosity) {
		this.verbose = verbosity;
	}

}
