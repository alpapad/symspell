package com.faroo.symspell.impl.v6;

import java.util.Arrays;

/**
 * Computes and returns the Damerau-Levenshtein edit distance between two
 * strings, i.e. the number of insertion, deletion, sustitution, and
 * transposition edits required to transform one string to the other. This value
 * will be >= 0, where 0 indicates identical strings. Comparisons are case
 * sensitive, so for example, "Fred" and "fred" will have a distance of 1. This
 * algorithm is basically the Levenshtein algorithm with a modification that
 * considers transposition of two adjacent characters as a single edit.
 * http://blog.softwx.net/2015/01/optimizing-damerau-levenshtein_15.html
 * https://github.com/softwx/SoftWx.Match
 * 
 * 
 * See http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance This is
 * inspired by Sten Hjelmqvist'string1 "Fast, memory efficient" algorithm,
 * described at
 * http://www.codeproject.com/Articles/13525/Fast-memory-efficient-Levenshtein-algorithm.
 * This version differs by adding additional optimizations, and extending it to
 * the Damerau- Levenshtein algorithm. Note that this is the simpler and faster
 * optimal string alignment (aka restricted edit) distance that difers slightly
 * from the classic Damerau-Levenshtein algorithm by imposing the restriction
 * that no substring is edited more than once. So for example, "CA" to "ABC" has
 * an edit distance of 2 by a complete application of Damerau-Levenshtein, but a
 * distance of 3 by this method that uses the optimal string alignment
 * algorithm. See wikipedia article for more detail on this distinction.
 * 
 * 
 * 
 * <license> The MIT License (MIT)
 * 
 * Copyright(c) 2015 Steve Hatchett
 * 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files(the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. </license>
 */
public class EditDistance {

	// Supported edit distance algorithms.
	public enum DistanceAlgorithm {
		Damerau
	}

	private String baseString;
	private DistanceAlgorithm algorithm;
	private int[] v0;
	private int[] v2;

	/**
	 * Create a new EditDistance object.
	 * 
	 * @param baseString
	 *            The base string to which other strings will be compared
	 * @param algorithm
	 *            The desired edit distance algorithm
	 */
	public EditDistance(String baseString, DistanceAlgorithm algorithm) {
		this.baseString = baseString;
		this.algorithm = algorithm;
		if (this.baseString == "") {
			this.baseString = null;
			return;
		}
		if (algorithm == DistanceAlgorithm.Damerau) {
			v0 = new int[baseString.length()];
			v2 = new int[baseString.length()]; // stores one level further back (offset by +1 position)
		}
	}

	/**
	 * Compare a string to the base string to determine the edit distance, using the
	 * previously selected algorithm.
	 * 
	 * @param string2
	 *            The string to compare.
	 * @param maxDistance
	 *            The maximum distance allowed.
	 * @return The edit distance (or -1 if maxDistance exceeded).
	 */
	public int Compare(String string2, int maxDistance) {
		switch (algorithm) {
		case Damerau:
			return damerauLevenshteinDistance(string2, maxDistance);
		}
		throw new IllegalArgumentException("unknown DistanceAlgorithm");
	}

	/**
	 * stores one level further back (offset by +1 position)
	 * 
	 * @param string2
	 *            String being compared for distance.
	 * @param maxDistance
	 *            The maximum edit distance of interest.
	 * @return int edit distance, >= 0 representing the number of edits required to
	 *         transform one string to the other, or -1 if the distance is greater
	 *         than the specified maxDistance.
	 */
	public int damerauLevenshteinDistance(String string2, int maxDistance) {
		if (baseString == null) {
			return (string2 != null ? string2 : "").length();
		}
		if ((string2 == null) || string2.trim().isEmpty()) {
			return baseString.length();
		}

		// if strings of different lengths, ensure shorter string is in string1. This
		// can result in a little
		// faster speed by spending more time spinning just the inner loop during the
		// main processing.
		String string1;
		if (baseString.length() > string2.length()) {
			string1 = string2;
			string2 = baseString;
		} else {
			string1 = baseString;
		}
		int sLen = string1.length(); // this is also the minimun length of the two strings
		int tLen = string2.length();

		// suffix common to both strings can be ignored
		while ((sLen > 0) && (string1.charAt(sLen - 1) == string2.charAt(tLen - 1))) {
			sLen--;
			tLen--;
		}

		int start = 0;
		if ((string1.charAt(0) == string2.charAt(0)) || (sLen == 0)) { // if there'string1 a shared prefix, or all
																		// string1 matches string2'string1 suffix
			// prefix common to both strings can be ignored
			while ((start < sLen) && (string1.charAt(start) == string2.charAt(start))) {
				start++;
			}
			sLen -= start; // length of the part excluding common prefix and suffix
			tLen -= start;

			// if all of shorter string matches prefix and/or suffix of longer string, then
			// edit distance is just the delete of additional characters present in longer
			// string
			if (sLen == 0) {
				return tLen;
			}
			// System.err.println(string2 + " " + start + " " + tLen);
			string2 = string2.substring(start, start + tLen); // faster than string2[start+j] in inner loop below
		}
		int lenDiff = tLen - sLen;
		if ((maxDistance < 0) || (maxDistance > tLen)) {
			maxDistance = tLen;
		} else if (lenDiff > maxDistance) {
			return -1;
		}

		if (tLen > v0.length) {
			v0 = new int[tLen];
			v2 = new int[tLen];
		} else {
			Arrays.fill(v2, 0, tLen, 0);
		}
		int j;
		for (j = 0; j < maxDistance; j++) {
			v0[j] = j + 1;
		}
		for (; j < tLen; j++) {
			v0[j] = maxDistance + 1;
		}

		int jStartOffset = maxDistance - (tLen - sLen);
		boolean haveMax = maxDistance < tLen;
		int jStart = 0;
		int jEnd = maxDistance;
		char sChar = string1.charAt(0);
		int current = 0;
		for (int i = 0; i < sLen; i++) {
			char prevsChar = sChar;
			sChar = string1.charAt(start + i);
			char tChar = string2.charAt(0);
			int left = i;
			current = left + 1;
			int nextTransCost = 0;
			// no need to look beyond window of lower right diagonal - maxDistance cells
			// (lower right diag is i - lenDiff)
			// and the upper left diagonal + maxDistance cells (upper left is i)
			jStart += (i > jStartOffset) ? 1 : 0;
			jEnd += (jEnd < tLen) ? 1 : 0;
			// jEnd= Math.min(jEnd, string2.length());

			for (j = jStart; j < jEnd; j++) {
				int above = current;
				int thisTransCost = nextTransCost;
				nextTransCost = v2[j];
				v2[j] = current = left; // cost of diagonal (substitution)
				left = v0[j]; // left now equals current cost (which will be diagonal at next iteration)
				char prevtChar = tChar;
				tChar = string2.charAt(j);
				if (sChar != tChar) {
					if (left < current) {
						current = left; // insertion
					}
					if (above < current) {
						current = above; // deletion
					}
					current++;
					if ((i != 0) && (j != 0) && (sChar == prevtChar) && (prevsChar == tChar)) {
						thisTransCost++;
						if (thisTransCost < current) {
							current = thisTransCost; // transposition
						}
					}
				}
				v0[j] = current;
			}
			if (haveMax && (v0[i + lenDiff] > maxDistance)) {
				return -1;
			}
		}
		return (current <= maxDistance) ? current : -1;
	}
}