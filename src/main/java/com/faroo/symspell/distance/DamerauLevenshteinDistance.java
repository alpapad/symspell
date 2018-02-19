package com.faroo.symspell.distance;

import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;

public class DamerauLevenshteinDistance implements IDistance {

	@Override
	public int distance(char[] a, char[] b, int maxDistance) {

		final int inf = a.length + b.length + 1;
		int[][] H = new int[a.length + 2][b.length + 2];
		for (int i = 0; i <= a.length; i++) {
			H[i + 1][1] = i;
			H[i + 1][0] = inf;
		}
		for (int j = 0; j <= b.length; j++) {
			H[1][j + 1] = j;
			H[0][j + 1] = inf;
		}
		TCharIntMap DA = new TCharIntHashMap();
		for (int d = 0; d < a.length; d++) {
			if (!DA.containsKey(a[d])) {
				DA.put(a[d], 0);
			}
		}

		for (int d = 0; d < b.length; d++) {
			if (!DA.containsKey(b[d])) {
				DA.put(b[d], 0);
			}
		}

		for (int i = 1; i <= a.length; i++) {
			int DB = 0;
			for (int j = 1; j <= b.length; j++) {
				final int i1 = DA.get(b[j - 1]);
				final int j1 = DB;
				int d = 1;
				if (a[i - 1] == b[j - 1]) {
					d = 0;
					DB = j;
				}
				H[i + 1][j + 1] = min(H[i][j] + d, H[i + 1][j] + 1, H[i][j + 1] + 1,
						H[i1][j1] + ((i - i1 - 1)) + 1 + ((j - j1 - 1)));
			}
			DA.put(a[i - 1], i);
		}
		return H[a.length + 1][b.length + 1];
	}

	public int min(int a, int b, int c, int d) {
		return Math.min(a, Math.min(b, Math.min(c, d)));
	}
}
