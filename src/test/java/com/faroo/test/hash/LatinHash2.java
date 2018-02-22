package com.faroo.test.hash;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class LatinHash2 {

	static final long[][] key = {
			{ 0x0L, 0x1L, 0x2L, 0x3L, 0x4L, 0x5L, 0x6L, 0x7L, 0x8L, 0x9L, 0xaL, 0xbL, 0xcL, 0xdL, 0xeL, 0xfL, 0x10L,
					0x11L, 0x12L, 0x13L, 0x14L, 0x15L, 0x16L, 0x17L, 0x18L, 0x19L, 0x1aL, }, //
			{ 0x0L, 0x1bL, 0x36L, 0x51L, 0x6cL, 0x87L, 0xa2L, 0xbdL, 0xd8L, 0xf3L, 0x10eL, 0x129L, 0x144L, 0x15fL,
					0x17aL, 0x195L, 0x1b0L, 0x1cbL, 0x1e6L, 0x201L, 0x21cL, 0x237L, 0x252L, 0x26dL, 0x288L, 0x2a3L,
					0x2beL, }, //
			{ 0x0L, 0x2d9L, 0x5b2L, 0x88bL, 0xb64L, 0xe3dL, 0x1116L, 0x13efL, 0x16c8L, 0x19a1L, 0x1c7aL, 0x1f53L,
					0x222cL, 0x2505L, 0x27deL, 0x2ab7L, 0x2d90L, 0x3069L, 0x3342L, 0x361bL, 0x38f4L, 0x3bcdL, 0x3ea6L,
					0x417fL, 0x4458L, 0x4731L, 0x4a0aL, }, //
			{ 0x0L, 0x4ce3L, 0x99c6L, 0xe6a9L, 0x1338cL, 0x1806fL, 0x1cd52L, 0x21a35L, 0x26718L, 0x2b3fbL, 0x300deL,
					0x34dc1L, 0x39aa4L, 0x3e787L, 0x4346aL, 0x4814dL, 0x4ce30L, 0x51b13L, 0x567f6L, 0x5b4d9L, 0x601bcL,
					0x64e9fL, 0x69b82L, 0x6e865L, 0x73548L, 0x7822bL, 0x7cf0eL, }, //
			{ 0x0L, 0x81bf1L, 0x1037e2L, 0x1853d3L, 0x206fc4L, 0x288bb5L, 0x30a7a6L, 0x38c397L, 0x40df88L, 0x48fb79L,
					0x51176aL, 0x59335bL, 0x614f4cL, 0x696b3dL, 0x71872eL, 0x79a31fL, 0x81bf10L, 0x89db01L, 0x91f6f2L,
					0x9a12e3L, 0xa22ed4L, 0xaa4ac5L, 0xb266b6L, 0xba82a7L, 0xc29e98L, 0xcaba89L, 0xd2d67aL, }, //
			{ 0x0L, 0xdaf26bL, 0x1b5e4d6L, 0x290d741L, 0x36bc9acL, 0x446bc17L, 0x521ae82L, 0x5fca0edL, 0x6d79358L,
					0x7b285c3L, 0x88d782eL, 0x9686a99L, 0xa435d04L, 0xb1e4f6fL, 0xbf941daL, 0xcd43445L, 0xdaf26b0L,
					0xe8a191bL, 0xf650b86L, 0x103ffdf1L, 0x111af05cL, 0x11f5e2c7L, 0x12d0d532L, 0x13abc79dL,
					0x1486ba08L, 0x1561ac73L, 0x163c9edeL, }, //
			{ 0x0L, 0x17179149L, 0x2e2f2292L, 0x4546b3dbL, 0x5c5e4524L, 0x7375d66dL, 0x8a8d67b6L, 0xa1a4f8ffL,
					0xb8bc8a48L, 0xcfd41b91L, 0xe6ebacdaL, 0xfe033e23L, 0x1151acf6cL, 0x12c3260b5L, 0x14349f1feL,
					0x15a618347L, 0x171791490L, 0x18890a5d9L, 0x19fa83722L, 0x1b6bfc86bL, 0x1cdd759b4L, 0x1e4eeeafdL,
					0x1fc067c46L, 0x2131e0d8fL, 0x22a359ed8L, 0x2414d3021L, 0x25864c16aL, }, //
			{ 0x0L, 0x26f7c52b3L, 0x4def8a566L, 0x74e74f819L, 0x9bdf14accL, 0xc2d6d9d7fL, 0xe9ce9f032L, 0x110c6642e5L,
					0x137be29598L, 0x15eb5ee84bL, 0x185adb3afeL, 0x1aca578db1L, 0x1d39d3e064L, 0x1fa9503317L,
					0x2218cc85caL, 0x248848d87dL, 0x26f7c52b30L, 0x2967417de3L, 0x2bd6bdd096L, 0x2e463a2349L,
					0x30b5b675fcL, 0x332532c8afL, 0x3594af1b62L, 0x38042b6e15L, 0x3a73a7c0c8L, 0x3ce324137bL,
					0x3f52a0662eL, }, //
			{ 0x0L, 0x41c21cb8e1L, 0x83843971c2L, 0xc546562aa3L, 0x1070872e384L, 0x148ca8f9c65L, 0x18a8cac5546L,
					0x1cc4ec90e27L, 0x20e10e5c708L, 0x24fd3027fe9L, 0x291951f38caL, 0x2d3573bf1abL, 0x3151958aa8cL,
					0x356db75636dL, 0x3989d921c4eL, 0x3da5faed52fL, 0x41c21cb8e10L, 0x45de3e846f1L, 0x49fa604ffd2L,
					0x4e16821b8b3L, 0x5232a3e7194L, 0x564ec5b2a75L, 0x5a6ae77e356L, 0x5e870949c37L, 0x62a32b15518L,
					0x66bf4ce0df9L, 0x6adb6eac6daL, }, //
			{ 0x0L, 0x6ef79077fbbL, 0xddef20eff76L, 0x14ce6b167f31L, 0x1bbde41dfeecL, 0x22ad5d257ea7L, 0x299cd62cfe62L,
					0x308c4f347e1dL, 0x377bc83bfdd8L, 0x3e6b41437d93L, 0x455aba4afd4eL, 0x4c4a33527d09L,
					0x5339ac59fcc4L, 0x5a2925617c7fL, 0x61189e68fc3aL, 0x680817707bf5L, 0x6ef79077fbb0L,
					0x75e7097f7b6bL, 0x7cd68286fb26L, 0x83c5fb8e7ae1L, 0x8ab57495fa9cL, 0x91a4ed9d7a57L,
					0x989466a4fa12L, 0x9f83dfac79cdL, 0xa67358b3f988L, 0xad62d1bb7943L, 0xb4524ac2f8feL, }, //
			{ 0x0L, 0xbb41c3ca78b9L, 0x176838794f172L, 0x231c54b5f6a2bL, 0x2ed070f29e2e4L, 0x3a848d2f45b9dL,
					0x4638a96bed456L, 0x51ecc5a894d0fL, 0x5da0e1e53c5c8L, 0x6954fe21e3e81L, 0x75091a5e8b73aL,
					0x80bd369b32ff3L, 0x8c7152d7da8acL, 0x98256f1482165L, 0xa3d98b5129a1eL, 0xaf8da78dd12d7L,
					0xbb41c3ca78b90L, 0xc6f5e00720449L, 0xd2a9fc43c7d02L, 0xde5e18806f5bbL, 0xea1234bd16e74L,
					0xf5c650f9be72dL, 0x1017a6d3665fe6L, 0x10d2e89730d89fL, 0x118e2a5afb5158L, 0x12496c1ec5ca11L,
					0x1304ade29042caL, }, //
			{ 0x0L, 0x13bfefa65abb83L, 0x277fdf4cb57706L, 0x3b3fcef3103289L, 0x4effbe996aee0cL, 0x62bfae3fc5a98fL,
					0x767f9de6206512L, 0x8a3f8d8c7b2095L, 0x9dff7d32d5dc18L, 0xb1bf6cd930979bL, 0xc57f5c7f8b531eL,
					0xd93f4c25e60ea1L, 0xecff3bcc40ca24L, 0x100bf2b729b85a7L, 0x1147f1b18f6412aL, 0x1283f0abf50fcadL,
					0x13bfefa65abb830L, 0x14fbeea0c0673b3L, 0x1637ed9b2612f36L, 0x1773ec958bbeab9L, 0x18afeb8ff16a63cL,
					0x19ebea8a57161bfL, 0x1b27e984bcc1d42L, 0x1c63e87f226d8c5L, 0x1d9fe7798819448L, 0x1edbe673edc4fcbL,
					0x2017e56e5370b4eL, }, //
			{ 0x0L, 0x2153e468b91c6d1L, 0x42a7c8d17238da2L, 0x63fbad3a2b55473L, 0x854f91a2e471b44L, 0xa6a3760b9d8e215L,
					0xc7f75a7456aa8e6L, 0xe94b3edd0fc6fb7L, 0x10a9f2345c8e3688L, 0x12bf307ae81ffd59L,
					0x14d46ec173b1c42aL, 0x16e9ad07ff438afbL, 0x18feeb4e8ad551ccL, 0x1b1429951667189dL,
					0x1d2967dba1f8df6eL, 0x1f3ea6222d8aa63fL, 0x2153e468b91c6d10L, 0x236922af44ae33e1L,
					0x257e60f5d03ffab2L, 0x27939f3c5bd1c183L, 0x29a8dd82e7638854L, 0x2bbe1bc972f54f25L,
					0x2dd35a0ffe8715f6L, 0x2fe898568a18dcc7L, 0x31fdd69d15aaa398L, 0x341314e3a13c6a69L,
					0x3628532a2cce313aL, }, //

	};

	static Map<Long, String> found = new HashMap<>();

	public static void main(String[] args) {
		// String a = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
		// char[]x = a.toCharArray();
		// long hash = 0;

		// System.err.println("aaaaaaaaaaaaaaaaaat".length());
		long[][] map = new long[27][];
		for (int i = 0; i < 27; i++) {
			map[i] = new long[100];
		}
		BigInteger b26 = BigInteger.valueOf(27);

		for (int i = 0; i < 27; i++) {
			for (int j = 0; j < 27; j++) {

				BigInteger x = b26.pow(i);
				BigInteger f = x.multiply(BigInteger.valueOf(j)).subtract(BigInteger.valueOf(Long.MIN_VALUE));

				if (f.bitLength() < 65) {
					map[i][j] = f.longValue();
				}
			}
		}
		for (int i = 0; i < 27; i++) {
			System.err.print("{");
			for (int j = 0; j < 27; j++) {
				System.err.print("0x" + Long.toHexString(map[i][j]) + "L,");
			}
			System.err.println("}, //");
			// System.err.println(Arrays.toString(map[i]));
		}
		// for(int i =0; i<27;i ++) {
		// StringBuilder sb = new StringBuilder();
		// sb.append((char) ('a' +i));
		// System.err.println(i + "->" + sb.toString());
		// }
		//
		// System.err.println(hash("byneq") + " " + hash("baoeq"));
		// found 922 for accsl colliding with: aaaiz
		addHash("", 0);

		// for (int i = 0; i < 20; i++) {
		// char t = (char) ('a' + i);
		// StringBuilder sb = new StringBuilder("a");
		// sb.append(t);
		// String aa = sb.toString();
		// System.err.println(i + " -> " + hash(aa) + " --->" + aa);
		// }
		//
		// StringBuilder sb = new StringBuilder("");
		// for (int i = 0; i < 20; i++) {
		// for (char a = 'a'; a <= 'z'; a++) {
		// hash(sb.toString() + a);
		// }
		//
		// }

	}

	static void addHash(String sb, int depth) {
		if (depth < 11) {
			for (char a = 'a'; a <= ('a' + 25); a++) {
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
		long hash = Long.MIN_VALUE;

		for (int i = (x.length - 1); i >= 0; i--) {
			int c = (x[i] - 'a' + 1);
			if (c < 0 | c > 26) {
				c = 0;
			}

			// double mult = c * Math.pow(24, i);
			// System.err.println(x[i] + " " + i + " " + c + " --> " + mult);
			hash += key[i % 12][c];
		}
		// System.err.println();
		return hash;
	}
}
