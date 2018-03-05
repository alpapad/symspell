/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell.impl.v3;

import static com.faroo.symspell.hash.XxHash64.hash;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.faroo.symspell.Verbosity;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

/**
 * HashMapDictionary that contains both the original words and the deletes
 * derived from them. A term might be both word and delete from another word at
 * the same time.
 */
public class TroveCompactWordIndex implements IWordIndex, Externalizable {

	/**
	 * HashMapDictionary that contains both the original words and the deletes
	 * derived from them. A term might be both word and delete from another word at
	 * the same time.
	 * 
	 * Instead of keeping the words as pointers, we use a very low collision hash of
	 * the word as a key.
	 * 
	 * For space reduction a item might be either of type of the values can be
	 * either an integer or a DictionaryItem
	 * 
	 * A DictionaryItem is used for word, word/delete, and delete with multiple
	 * suggestions. integer is used for deletes with a single suggestion (the
	 * majority of entries).
	 */
	private final TLongObjectHashMap<Object> dictionary = new TLongObjectHashMap<>(100_000, 1f);

	private int wordCount = 0;

	private int restricetdEditDistanceMax = 2;

	public int maxlength = 0;// maximum tempDictionary term length

	private final ThreadLocal<CompactMatchesIterator> it = new ThreadLocal<>();

	public TroveCompactWordIndex(int editDistanceMax, Verbosity verbosity) {
		super();
		this.restricetdEditDistanceMax = editDistanceMax;
	}



	private static final Object[] WORD = new Object[] { null };

	/**
	 * For every word there all deletes with an edit distance of 1..editDistanceMax
	 * created and added to the tempDictionary every delete entry has a suggestions
	 * list, which points to the original term(s) it was created from
	 * 
	 * The tempDictionary may be dynamically updated (word frequency and new words)
	 * at any time by calling createDictionaryEntry
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public boolean createDictionaryEntry(String key) {
		key = key.intern();
		boolean result = false;
		final long kh = hash(key);

		Object current = dictionary.get(kh);
		boolean newKey = false;
		if (current != null) {
			// String or array? If string, then delete existed before word!
			if (current instanceof String) {
				dictionary.put(kh, new Object[] { null, current });
				newKey = true;
			} else {
				// already exists:
				// 1. word appears several times
				// 2. word1==deletes(word2)
				Object[] value = (Object[]) current;
				// word1==deletes(word2)
				if (value[0] != null) {
					newKey = true;
					dictionary.put(kh, prepend(null, value));
				}
			}

		} else {
			newKey = true;
			dictionary.put(kh, WORD);
			if (key.length() > maxlength) {
				maxlength = key.length();
			}
		}

		/*
		 * edits/suggestions are created only once, no matter how often word occurs
		 * 
		 * edits/suggestions are created only as soon as the word occurs in the corpus,
		 * 
		 * even if the same term existed before in the tempDictionary as an edit from //
		 * another word
		 * 
		 * a treshold might be specifid, when a term occurs so frequently in the corpus
		 * that it is considered a valid word for spelling correction
		 */
		if (newKey) {
			wordCount++;
			result = true;
			final int maxEditDist = this.restricetdEditDistanceMax;// Math.min(dist(key), this.restricetdEditDistanceMax);
			// create deletes
			for (String delete : edits(key, maxEditDist)) {
				final long hd = hash(delete);

				Object value2 = dictionary.get(hd);
				if (value2 != null) {
					// already exists:
					// 1. word1==deletes(word2)
					// 2. deletes(word1)==deletes(word2)

					// String or array? single delete existed before!
					if (value2 instanceof String) {
						if (!value2.equals(key)) {
							dictionary.put(hd, new Object[] { value2, key });
						}
					} else if (!contains(key, (Object[]) value2)) {
						dictionary.put(hd, append((Object[]) value2, key));
					}
				} else {
					dictionary.put(hd, key);
				}

			}
		}
		return result;
	}

	// save some time and space
	// private void addLowestDistance(Node item, String suggestion, String delete) {
	// // remove all existing suggestions of higher distance, if verbose<2
	// index2word
	// // TODO check
	// if ((verbose < 2) && (item.suggestions.size() > 0)
	// && ((item.suggestions.get(0).length() - delete.length()) >
	// (suggestion.length() - delete.length()))) {
	// item.suggestions.clear();
	// }
	// // do not add suggestion of higher distance than existing, if verbose<2
	// if ((verbose == 2) || (item.suggestions.size() == 0)
	// || ((item.suggestions.get(0).length() - delete.length()) >=
	// (suggestion.length() - delete.length()))) {
	// item.suggestions.add(suggestion);
	// }
	// }

	@Override
	public int getMaxLength() {
		return maxlength;
	}

	@Override
	public DictionaryItem getEntry(String candidate) {
		return null;
	}

	@Override
	public IMatchingItemsIterator getMatches(String candidate, IMatchingItemsIterator item) {
		Object dictionaryEntry = this.dictionary.get(hash(candidate));
		if (dictionaryEntry != null) {
			return CompactMatchesIterator.class.cast(item).init(dictionaryEntry);
		}
		return null;
	}

	@Override
	public void commit() {
		dictionary.compact();
	}

	@Override
	public int getWordCount() {
		return wordCount;
	}

	@Override
	public int getEntryCount() {
		return dictionary.size();
	}

	private static boolean contains(Object value, Object[] array) {
		if (array == null || array.length == 0) {
			return false;
		}
		for (Object o : array) {
			if (o != null && o.equals(value)) {
				return true;
			}
		}
		return false;
	}

	private static Object[] prepend(Object a, Object[] array) {
		Object[] joinedArray = new Object[array.length + 1];
		joinedArray[0] = a;
		System.arraycopy(array, 0, joinedArray, 1, array.length);
		assert joinedArray[0] == a;
		return joinedArray;
	}

	private static Object[] append(Object[] array, Object a) {
		Object[] joinedArray = new Object[array.length + 1];
		joinedArray[array.length] = a;
		System.arraycopy(array, 0, joinedArray, 0, array.length);
		assert joinedArray[array.length] == a;
		return joinedArray;
	}

	@Override
	public IMatchingItemsIterator getIterable() {
		CompactMatchesIterator ter = it.get();
		if (ter == null) {
			ter = new CompactMatchesIterator();
			it.set(ter);
		}
		return ter;
	}

	private final static byte STRING_VAL = 2;
	private final static byte NULL_PLUS_STRING_ARR_VAL = 1;
	private final static byte STRING_ARR_VAL = 0;
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeByte(0);
		out.writeInt(wordCount);
		out.writeInt(restricetdEditDistanceMax);
		out.writeInt(maxlength);
		this.dictionary.compact();
		out.writeInt(dictionary.size());
		this.dictionary.forEachEntry(new TLongObjectProcedure<Object>() {
			@Override
			public boolean execute(long key, Object value) {
				try {
					if (value instanceof String) {
						out.writeLong(key); // key
						out.writeInt(1); // len
						out.writeByte(STRING_VAL); // type = string
						out.writeUTF(String.class.cast(value)); // value
					} else {
						final Object[] values = Object[].class.cast(value);
						if (values.length > 0) {
							out.writeLong(key); // key
							out.writeInt(values.length); // len
							int start = 0;
							if (values[0] == null) {
								out.writeByte(NULL_PLUS_STRING_ARR_VAL); // type = null + arr string
								start = 1;
							} else {
								out.writeByte(STRING_ARR_VAL); // type = arr string
							}

							for (int i = start; i < values.length; i++) {
								String ss = String.class.cast(values[i]);
								out.writeUTF(ss);
							}
						}
					}
				} catch (IOException e) {
				    throw new RuntimeException(e);
				}
				return true;
			}
		});
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// VERSION
		in.readByte();
		wordCount = in.readInt();
		restricetdEditDistanceMax = in.readInt();
		maxlength = in.readInt();
		final int size = in.readInt();

		dictionary.clear();
		for (int i = 0; i < size; i++) {
			final long key = in.readLong();
			final int itemLen = in.readInt();

			if (itemLen > 0) {
				final byte valueType = in.readByte();
				if (itemLen == 1 && valueType == STRING_VAL) {
					dictionary.put(key, in.readUTF().intern());
				} else {
					final Object[] arr = new Object[itemLen];
					int start = 0;
					if(valueType == NULL_PLUS_STRING_ARR_VAL) {
					    arr[0] = null;
					    start = 1;
					}else {
					    assert valueType == STRING_ARR_VAL;
					}
					for (int k = start; k < itemLen; k++) {
						arr[k] = in.readUTF().intern();
					}
					dictionary.put(key, arr);
				}
			} else {
				throw new RuntimeException("Empty?");
			}
		}
		dictionary.compact();
		assert size == dictionary.size();
	}
}
