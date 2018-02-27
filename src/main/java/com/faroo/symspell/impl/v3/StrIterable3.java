package com.faroo.symspell.impl.v3;

import java.util.Iterator;

class StrIterable3 implements IDictionaryItems {

    private final Object[] items;
    private final int count;
    private final int start;
    public StrIterable3(Object[] items) {
        super();
        
        if(items[0] instanceof Integer) {
            this.count = Integer.class.cast(items[0]).intValue();
            this.start = 1;
        } else {
            this.count = 0;
            this.start = 0;
        }
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int idx = start;
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
