package com.faroo.symspell.map;

import java.util.Arrays;


/**
 * Object-2-object map based on IntIntMap4a
 * http://java-performance.info/implementing-world-fastest-java-int-to-int-hash-map/
 */
@SuppressWarnings("unchecked")
public class LongObjMap<V> {

    /** Keys and values */
    private Object[] m_data;

    /** Fill factor, must be between (0 and 1) */
    private final float m_fillFactor;
    /** We will resize a map once it reaches this size */
    private int m_threshold;
    /** Current map size */
    private int m_size;
    /** Mask to calculate the original position */
    private int m_mask;
    /** Mask to wrap the actual array pointer */
    private int m_mask2;

    public LongObjMap(final int size, final float fillFactor) {
        if ((fillFactor <= 0) || (fillFactor >= 1)) {
            throw new IllegalArgumentException("FillFactor must be in (0, 1)");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive!");
        }
        final int capacity = Tools.arraySize(size, fillFactor);
        m_mask = capacity - 1;
        m_mask2 = (capacity * 2) - 1;
        m_fillFactor = fillFactor;

        m_data = new Object[capacity * 2];
        Arrays.fill(m_data, null);

        m_threshold = (int) (capacity * fillFactor);
    }

    public void shrink() {
        int nextSize = Tools.arraySize(m_size);
        int newCapacity = nextSize * 2;

        m_threshold = (int) (newCapacity * m_fillFactor);
        m_mask = (newCapacity / 2) - 1;
        m_mask2 = newCapacity - 1;

        final int oldCapacity = m_data.length;
        final Object[] oldData = m_data;
        
        m_data = new Object[newCapacity];

        Arrays.fill(m_data, null);
        m_size = 0;

        for (int i = 0; i < oldCapacity; i += 2) {
            if (oldData[i] != null) {
                put((long) oldData[i], (V) oldData[i + 1]);
            }
        }
    }

    public V get(final long key) {
        int ptr = (Long.hashCode(key) & m_mask) << 1;
        // end of chain already
        if (m_data[ptr] == null) {
            return null;
        }
        long k = (long) m_data[ptr];

        if (k == key) {// we check FREE and REMOVED prior to this call
            return (V) m_data[ptr + 1];
        }
        while (true) {
            ptr = (ptr + 2) & m_mask2; // that's next index
            if (m_data[ptr] == null) {
                return null;
            }
            k = (long) m_data[ptr];
            if (k == key) {
                return (V) m_data[ptr + 1];
            }
        }
    }

    public V put(final long key, final V value) {
        int ptr = getStartIndex(key) << 1;
        if (m_data[ptr] == null) {
            // end of chain already
            m_data[ptr] = key;
            m_data[ptr + 1] = value;
            if (m_size >= m_threshold) {
                rehash(m_data.length * 2); // size is set inside
            } else {
                ++m_size;
            }
            return null;
        } else if (((long) m_data[ptr]) == key) {
            // we check FREE prior to this call
            final Object ret = m_data[ptr + 1];
            m_data[ptr + 1] = value;
            return (V) ret;
        }

        while (true) {
            ptr = (ptr + 2) & m_mask2; // that's next index calculation
            if (m_data[ptr] == null) {

                m_data[ptr] = key;
                m_data[ptr + 1] = value;
                if (m_size >= m_threshold) {
                    rehash(m_data.length * 2); // size is set inside
                } else {
                    ++m_size;
                }
                return null;
            } else if (((long) m_data[ptr]) == key) {
                final Object ret = m_data[ptr + 1];
                m_data[ptr + 1] = value;
                return (V) ret;
            } 
        }
    }


    public int size() {
        return m_size;
    }

    private void rehash(final int newCapacity) {
        m_threshold = (int) ((newCapacity / 2) * m_fillFactor);
        m_mask = (newCapacity / 2) - 1;
        m_mask2 = newCapacity - 1;

        final int oldCapacity = m_data.length;
        final Object[] oldData = m_data;

        m_data = new Object[newCapacity];
        Arrays.fill(m_data, null);
        
        m_size = 0;

        for (int i = 0; i < oldCapacity; i += 2) {
            if (oldData[i] != null) {
                put((long) oldData[i], (V) oldData[i + 1]);
            }
        }
    }

    public void forEach(LongObjConsumer<? super V> action) {
        for (int i = 0; i < m_data.length; i += 2) {
            if (m_data[i] != null) {
                action.accept((long) m_data[i], (V) m_data[i + 1]);
            }
        }
    }

    public int getStartIndex(final long key) {
        // key is not null here
        return Long.hashCode(key) & m_mask;
    }
}
