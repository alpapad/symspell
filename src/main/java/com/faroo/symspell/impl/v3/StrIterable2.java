package com.faroo.symspell.impl.v3;

import java.util.Iterator;

class StrIterable2 implements IDictionaryItems {

    private final DictionaryItem items;
    private final boolean word;

    public StrIterable2(DictionaryItem items) {
        super();
        this.items = items;
        this.word = items.count >0 ;
    }

    @Override
    public boolean isWord() {
        return word;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < items.suggestions.length;
            }

            @Override
            public String next() {
                return String.class.cast(items.suggestions[idx++]);
            }
        };
    }
}
