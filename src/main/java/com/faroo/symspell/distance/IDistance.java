package com.faroo.symspell.distance;

import java.util.Arrays;

import com.faroo.symspell.string.StringToCharArr;

public interface IDistance {

    int distance(char[] ina, char[] inb, int maxDistance);

    default int distance(String left, char[] rightArr, int editDistanceMax) {
        // common prefixes and suffixes are ignored, because this speeds up the
        // Damerau-Levenshtein/Levenshtein Distance calculation without changing it.
        char[] leftArr = StringToCharArr.arr(left);//.toCharArray();
        ;
        int ii = 0;
        int jj = 0;
        while ((ii < leftArr.length) && (ii < rightArr.length) && (leftArr[ii] == rightArr[ii])) {
            ii++;
        }

        while ((jj < (leftArr.length - ii)) && (jj < (rightArr.length - ii)) && (leftArr[leftArr.length - jj - 1] == rightArr[rightArr.length - jj - 1])) {
            jj++;
        }

        if ((ii > 0) || (jj > 0)) {
            // FIXME: try to avoid array copy, adjust algos to use offset, len
            return distance(//
                    Arrays.copyOfRange(leftArr, ii, leftArr.length - jj), //
                    Arrays.copyOfRange(rightArr, ii, rightArr.length - jj), //
                    editDistanceMax);
        } else {
            return distance(leftArr, rightArr, editDistanceMax);
        }
    }

    default int distance(String left, String right, int editDistanceMax) {
        if ((left == null) && (right == null)) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        if (editDistanceMax < 0) {
            throw new IllegalArgumentException("editDistanceMax must not be negative");
        }

        if ((left == null) || left.isEmpty()) {
            return right.length();
        }

        if ((right == null) || right.isEmpty()) {
            return left.length();
        }

        if (left.equals(right)) {
            return 0;
        }

        // common prefixes and suffixes are ignored, because this speeds up the
        // Damerau-Levenshtein/Levenshtein Distance calculation without changing it.
        char[] leftArr = StringToCharArr.arr(left);//.toCharArray();
        char[] rightArr = StringToCharArr.arr(right);//.toCharArray();

        ;
        int ii = 0;
        int jj = 0;
        while ((ii < leftArr.length) && (ii < rightArr.length) && (leftArr[ii] == rightArr[ii])) {
            ii++;
        }

        while ((jj < (leftArr.length - ii)) && (jj < (rightArr.length - ii)) && (leftArr[leftArr.length - jj - 1] == rightArr[rightArr.length - jj - 1])) {
            jj++;
        }

        if ((ii > 0) || (jj > 0)) {
            return distance(//
                    Arrays.copyOfRange(leftArr, ii, leftArr.length - jj), //
                    Arrays.copyOfRange(rightArr, ii, rightArr.length - jj), //
                    editDistanceMax);
        } else {
            return distance(leftArr, rightArr, editDistanceMax);
        }
    }

    default double getPercentageDifference(CharSequence left, CharSequence right, double editDistance) {
        int longestWordLength = Math.max(left.length(), right.length());
        return 1.0d - ((editDistance / longestWordLength) * 1.0d);
    }

    default double getPercentageDifference(CharSequence keyword, double editDistance) {
        return 1.0d - (editDistance / keyword.length());
    }

    default int distanceForPct(int l, double k) {
        return (int) Math.round((1d - k) * l);
    }

    default int distanceForPct(int l1, int l2, double k) {
        return (int) Math.round((1d - k) * Math.max(l1, l2));
    }
}