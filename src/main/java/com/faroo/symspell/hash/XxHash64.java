package com.faroo.symspell.hash;

import net.openhft.hashing.LongHashFunction;

public class XxHash64 {
	static LongHashFunction hash = LongHashFunction.xx();
	
	public static long hash(String input) {
		return hash.hashChars(input);
	}
}
