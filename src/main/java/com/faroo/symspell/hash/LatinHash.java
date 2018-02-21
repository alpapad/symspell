package com.faroo.symspell.hash;

public class LatinHash {
	public static long hash(String a) {
		char[] x = a.toCharArray();
		long hash = 0;

		for (int i = 0; i < x.length; i++) {
			hash += ((x[i] - 'a') + 1) * (i > 0 ? (i * 24) : 1);
			// System.err.println( i + " -> " + hash);
		}
		return hash;
	}
}
