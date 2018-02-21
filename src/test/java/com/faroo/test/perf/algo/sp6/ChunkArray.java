package com.faroo.test.perf.algo.sp6;

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
import java.util.Arrays;

/**
 * A growable list of elements that's optimized to support adds, but not
 * deletes, of large numbers of elements, storing data in a way that's friendly
 * to the garbage collector (not backed by a monolithic array object), and can
 * grow without needing to copy the entire backing array contents from the old
 * backing array to the new.
 * 
 *
 * @param <T>
 */
class ChunkArray<T> {
	// this must be a power of 2, otherwise can't optimize Row and Col functions
	private final int chunkSize = 4096;

	// number of bits to shift right to do division by ChunkSize (the bit position
	// of ChunkSize)
	private final int divShift = 12;
	private Object[][] values;
	public int count;

	public ChunkArray(int initialCapacity) {
		int chunks = ((initialCapacity + chunkSize) - 1) / chunkSize;
		values = new Object[chunks][];
		for (int i = 0; i < values.length; i++) {
			values[i] = new Object[chunkSize];
		}
	}

	public int add(T value) {
		if (count == getCapacity()) {
			// T[][] newValues = (T[][]) new Object[Values.length + 1][];
			// only need to copy the list of array blocks, not the data in the blocks
			Object[][] newValues = Arrays.copyOf(values, values.length + 1);
			// Array.Copy(Values, newValues, Values.Length);
			newValues[values.length] = new Object[chunkSize];
			values = newValues;
		}
		values[getRow(count)][getCol(count)] = value;
		count++;
		return count - 1;
	}

	public void clear() {
		count = 0;
	}

	@SuppressWarnings("unchecked")
	T get(int index) {
		return (T) values[getRow(index)][getCol(index)];
	}

	private int getRow(int index) {
		return index >> divShift;
	} // same as index / ChunkSize

	private int getCol(int index) {
		return index & (chunkSize - 1);
	} // same as index % ChunkSize

	private int getCapacity() {
		return values.length * chunkSize;
	}
}