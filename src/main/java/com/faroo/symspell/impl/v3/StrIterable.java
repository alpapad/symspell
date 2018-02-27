package com.faroo.symspell.impl.v3;

import java.util.Iterator;

class StrIterable implements IDictionaryItems {

    private final Object[] items;
    private final int count;
    public StrIterable(Object[] items) {
        super();
        this.count = Integer.class.cast(items[0]).intValue();
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int idx = 1;
            @Override
            public boolean hasNext() {
                return idx < items.length;
            }
            @Override
            public String next() {
                return String.class.cast(items[idx++]);
            }
        };
    }
}
