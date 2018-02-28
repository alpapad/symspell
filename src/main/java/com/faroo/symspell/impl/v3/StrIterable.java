package com.faroo.symspell.impl.v3;

import java.util.Iterator;

class StrIterable implements IDictionaryItems {

    private Object[] items;
    private int count;
    private int start;
    private final Object[] single = new Object[1];
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
        if (items instanceof String) {
            this.count = 0;
            this.start = 0;
            this.items = single;
            this.single[0] = items;
        } else {
            this.items = (Object[]) items;
            if(this.items.length > 0 && this.items[0] == null) {
                this.count = 1;
                this.start = 1;
            } else {
            	this.count = 0;
            	this.start = 0;
            }
//            this.count = Integer.class.cast(this.items[0]).intValue();
//            this.start = 1;
        }
        return this;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Iterator<String> iterator() {
        return iter;
    }
}
