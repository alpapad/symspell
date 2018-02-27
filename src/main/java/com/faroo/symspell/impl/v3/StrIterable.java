package com.faroo.symspell.impl.v3;

import java.util.Iterator;

class StrIterable implements IDictionaryItems {

    private Object[] items;
    private int count;
    private int start;

    private Iterator<String> iter = new Iterator<String>() {
        @Override
        public boolean hasNext() {
            return start < items.length;
        }
        @Override
        public String next() {
            return String.class.cast(items[start++]);
        }
    };

    public StrIterable() {
    }

    public IDictionaryItems init(Object items) {
        if(items instanceof String) {
            this.count = 0;
            this.start = 0;
            this.items = new Object[]{items};
        } else {
            this.items = (Object[]) items;
            this.count = Integer.class.cast(this.items[0]).intValue();
            this.start = 1;
        }
        return this;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Iterator<String> iterator() {
        return iter;
    }
}
