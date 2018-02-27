package com.faroo.symspell.distance;

import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;

public class DamerauLevenshteinDistance implements IDistance {

    @Override
    public int distance(char[] a, char[] b, int maxDistance) {

        final int m = a.length;
        final int n = b.length;

        final int inf = m + n + 1;
        int[][] d = new int[m + 2][n + 2];
        for (int i = 0; i <= m; i++) {
            d[i + 1][1] = i;
            d[i + 1][0] = inf;
        }
        for (int j = 0; j <= n; j++) {
            d[1][j + 1] = j;
            d[0][j + 1] = inf;
        }

        TCharIntMap charDictionary = new TCharIntHashMap();
        for (int i = 0; i < m; i++) {
            charDictionary.put(a[i], 0);
        }

        for (int j = 0; j < n; j++) {
            charDictionary.put(b[j], 0);
        }

        for (int i = 1; i <= m; i++) {
            int db = 0;
            for (int j = 1; j <= n; j++) {
                final int i1 = charDictionary.get(b[j - 1]);
                final int j1 = db;
                int cost = 1;
                if (a[i - 1] == b[j - 1]) {
                    cost = 0;
                    db = j;
                }
                d[i + 1][j + 1] = Math.min(d[i][j] + cost, Math.min(d[i + 1][j] + 1, Math.min(d[i][j + 1] + 1, d[i1][j1] + ((i - i1 - 1)) + 1 + ((j - j1 - 1)))));
            }
            charDictionary.put(a[i - 1], i);
        }
        return d[m + 1][n + 1];
    }
}
