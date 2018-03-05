/*
 * The MIT License (MIT)

Copyright (c) 2017 DanHartley

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.faroo.symspell.distance;

/**
 * The fastest Levenshtein around.
 * 
 * Fastenshtein is an optimized and unit tested Levenshtein implementation. It is optimized for speed and memory usage.
 * 
 *
 * 
 * @author https://github.com/DanHarltey/Fastenshtein
 *
 */
public class FastenshteinLevDistance implements IDistance {

    @Override
    public int distance(char[] left, char[] right, int maxDistance) {
        if (right.length == 0) {
            return left.length;
        }

        final int arLen = right.length;

        final int[] costs = new int[arLen];

        // Add indexing for insertion to first row
        for (int i = 0; i < arLen;) {
            costs[i] = ++i;
        }
        final int inaLen = left.length;

        for (int i = 0; i < inaLen; i++) {
            // cost of the first index
            int cost = i;
            int additionCost = i;

            // cache value for inner loop to avoid index lookup and bonds checking, profiled this is quicker
            char value1Char = left[i];

            for (int j = 0; j < arLen; j++) {
                int insertionCost = cost;

                cost = additionCost;

                // assigning this here reduces the array reads we do, improvment of the old version
                additionCost = costs[j];

                if (value1Char != right[j]) {
                    if (insertionCost < cost) {
                        cost = insertionCost;
                    }
                    if (additionCost < cost) {
                        cost = additionCost;
                    }
                    ++cost;
                }
                costs[j] = cost;
            }
        }
        return costs[arLen - 1];
    }

}
