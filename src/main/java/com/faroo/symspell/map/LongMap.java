package com.faroo.symspell.map;

import java.io.IOException;
import java.io.ObjectInput;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class LongMap<V> extends TLongObjectHashMap<V> {

	public LongMap() {
		super();
	}

	public LongMap(int initialCapacity, float loadFactor, long noEntryKey) {
		super(initialCapacity, loadFactor, noEntryKey);
	}

	public LongMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public LongMap(int initialCapacity) {
		super(initialCapacity);
	}

	public LongMap(TLongObjectMap<? extends V> map) {
		super(map);
	}

	@SuppressWarnings({ "unchecked" })
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		// VERSION
		in.readByte();

		// From SUPER
		// super.readExternal(in);
		{
			in.readByte();
			// LOAD FACTOR
			_loadFactor = in.readFloat();

			// AUTO COMPACTION LOAD FACTOR
			_autoCompactionFactor = in.readFloat();

			// NO_ENTRY_KEY
			no_entry_key = in.readLong();
		}
		no_entry_key = in.readLong();
		
		// NUMBER OF ENTRIES
		int size = in.readInt();
		setUp(size);

		// ENTRIES
		while (size-- > 0) {
			long key = in.readLong();
			V val = (V) in.readObject();
			put(key, val);
		}
	}
}
