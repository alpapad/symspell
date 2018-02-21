package com.faroo.test.hash;

import java.util.HashMap;
import java.util.Map;

public class LatinHash {

	static Map<Long, String> found = new HashMap<>();

	public static void main(String[] args) {
		// String a = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
		// char[]x = a.toCharArray();
		// long hash = 0;

		System.err.println("aaaaaaaaaaaaaaaaaat".length());
		
		System.err.println(hash("byneq") + "   " + hash("baoeq"));
		//found 922 for accsl colliding with: aaaiz
		// addHash("", 0);
		 
//		for (int i = 0; i < 20; i++) {
//			char t = (char) ('a' + i);
//			StringBuilder sb = new StringBuilder("a");
//			sb.append(t);
//			String aa = sb.toString();
//			System.err.println(i + " -> " + hash(aa) + " --->" + aa);
//		}
//
//		StringBuilder sb = new StringBuilder("");
//		for (int i = 0; i < 20; i++) {
//			for (char a = 'a'; a <= 'z'; a++) {
//				hash(sb.toString() + a);
//			}
//
//		}

	}

	static void addHash(String sb, int depth) {
		if (depth < 5) {
			for (char a = 'a'; a <= ('a' + 24); a++) {
				long h = hash(sb.toString() + a);
				if (found.containsKey(h)) {
					System.err.println("found " + h + " for " + sb + a + " colliding with: " + found.get(h));
				} else {
					found.put(h, sb.toString() + a);
				}
				addHash(sb + a, depth + 1);
			}
		}
	}

	static long hash(String a) {
		char[] x = a.toCharArray();
		long hash = 0;

		for (int i = (x.length -1);  i >=0; i--) {
			int c = (x[i] - 'a'  +1) << 8;
			double  mult = c * Math.pow(24,i);
			System.err.println(x[i] + "  " + i + "  " + c + " --> " + mult);
			hash += mult;
		}
		System.err.println();
		return hash;
	}
}
