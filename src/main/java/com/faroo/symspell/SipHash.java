/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.faroo.symspell;

/**
 * A cryptographically relatively secure hash function. It is supposed to
 * protected against hash-flooding denial-of-service attacks.
 *
 */
public class SipHash {

	public static long hash(String in) {
		char[] chars = in.toCharArray();
		return getSipHash24(chars, 0, chars.length, in.hashCode(), chars.length);
	}

	/**
	 * A cryptographically relatively secure hash function. It is supposed to
	 * protected against hash-flooding denial-of-service attacks.
	 *
	 * @param b
	 *            the data
	 * @param start
	 *            the start position
	 * @param end
	 *            the end position plus one
	 * @param k0
	 *            key 0
	 * @param k1
	 *            key 1
	 * @return the hash value
	 */
	public static long getSipHash24(byte[] b, int start, int end, long k0, long k1) {
		long v0 = k0 ^ 0x736f6d6570736575L;
		long v1 = k1 ^ 0x646f72616e646f6dL;
		long v2 = k0 ^ 0x6c7967656e657261L;
		long v3 = k1 ^ 0x7465646279746573L;
		int repeat;
		for (int off = start; off <= end + 8; off += 8) {
			long m;
			if (off <= end) {
				m = 0;
				int i = 0;
				for (; i < 8 && off + i < end; i++) {
					m |= ((long) b[off + i] & 255) << (8 * i);
				}
				if (i < 8) {
					m |= ((long) end - start) << 56;
				}
				v3 ^= m;
				repeat = 2;
			} else {
				m = 0;
				v2 ^= 0xff;
				repeat = 4;
			}
			for (int i = 0; i < repeat; i++) {
				v0 += v1;
				v2 += v3;
				v1 = Long.rotateLeft(v1, 13);
				v3 = Long.rotateLeft(v3, 16);
				v1 ^= v0;
				v3 ^= v2;
				v0 = Long.rotateLeft(v0, 32);
				v2 += v1;
				v0 += v3;
				v1 = Long.rotateLeft(v1, 17);
				v3 = Long.rotateLeft(v3, 21);
				v1 ^= v2;
				v3 ^= v0;
				v2 = Long.rotateLeft(v2, 32);
			}
			v0 ^= m;
		}
		return v0 ^ v1 ^ v2 ^ v3;
	}

	/**
	 * A cryptographically relatively secure hash function. It is supposed to
	 * protected against hash-flooding denial-of-service attacks.
	 *
	 * @param b
	 *            the data
	 * @param start
	 *            the start position
	 * @param end
	 *            the end position plus one
	 * @param k0
	 *            key 0
	 * @param k1
	 *            key 1
	 * @return the hash value
	 */
	public static long getSipHash24(char[] b, int start, int end, long k0, long k1) {
		long v0 = k0 ^ 0x736f6d6570736575L;
		long v1 = k1 ^ 0x646f72616e646f6dL;
		long v2 = k0 ^ 0x6c7967656e657261L;
		long v3 = k1 ^ 0x7465646279746573L;
		int repeat;
		for (int off = start; off <= end + 8; off += 8) {
			long m;
			if (off <= end) {
				m = 0;
				int i = 0;
				for (; i < 8 && off + i < end; i++) {
					m |= ((long) b[off + i] & 255) << (8 * i);
				}
				if (i < 8) {
					m |= ((long) end - start) << 56;
				}
				v3 ^= m;
				repeat = 2;
			} else {
				m = 0;
				v2 ^= 0xff;
				repeat = 4;
			}
			for (int i = 0; i < repeat; i++) {
				v0 += v1;
				v2 += v3;
				v1 = Long.rotateLeft(v1, 13);
				v3 = Long.rotateLeft(v3, 16);
				v1 ^= v0;
				v3 ^= v2;
				v0 = Long.rotateLeft(v0, 32);
				v2 += v1;
				v0 += v3;
				v1 = Long.rotateLeft(v1, 17);
				v3 = Long.rotateLeft(v3, 21);
				v1 ^= v2;
				v3 ^= v0;
				v2 = Long.rotateLeft(v2, 32);
			}
			v0 ^= m;
		}
		return v0 ^ v1 ^ v2 ^ v3;
	}
}
