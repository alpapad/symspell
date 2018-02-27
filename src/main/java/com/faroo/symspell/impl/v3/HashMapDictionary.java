/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell.impl.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.faroo.symspell.Verbosity;

/**
 * HashMapDictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
 */
public class HashMapDictionary implements IDictionary {
    private HashMap<String, Object> tempDictionary = new HashMap<>();

    /**
     * HashMapDictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
     *
     * Instead of keeping the words as pointers, we use a very low collision hash of the word as a key.
     *
     * For space reduction a item might be either of type of the values can be either an integer or a DictionaryItem
     *
     * A DictionaryItem is used for word, word/delete, and delete with multiple suggestions. integer is used for deletes with a single suggestion (the majority of entries).
     */
    private HashMap<String, Object> dictionary = null;

    private int verbose = 2;
    private int restricetdEditDistanceMax = 2;

    public int maxlength = 0;// maximum tempDictionary term length

    private int size;

    private static class Node {
        public List<String> suggestions = new ArrayList<>();
        public int count = 0;
    }

    public HashMapDictionary(int editDistanceMax, Verbosity verbosity) {
        super();
        this.restricetdEditDistanceMax = editDistanceMax;
        this.verbose = verbosity.verbose;
    }

    static int dist(int l, double k) {
        return (int) Math.round((1d - k) * l);
    }

    /**
     * For every word there all deletes with an edit distance of 1..editDistanceMax created and added to the tempDictionary every delete entry has a suggestions list, which points to the original term(s) it was created from
     *
     * The tempDictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry
     *
     * @param key
     * @return
     */
    @Override
    public boolean createDictionaryEntry(String key) {
        boolean result = false;
        Node value = null;
        Object valueo = tempDictionary.get(key);
        if (valueo != null) {
            // int or dictionaryItem? delete existed before word!
            if (valueo instanceof String) {
                String tmp = String.class.cast(valueo);
                value = new Node();
                value.suggestions.add(tmp);
                tempDictionary.put(key, value);
            } else {
                // already exists:
                // 1. word appears several times
                // 2. word1==deletes(word2)
                value = Node.class.cast(valueo);
            }

            // prevent overflow
            if (value.count < Integer.MAX_VALUE) {
                value.count++;
            }
        } else {
            value = new Node();
            value.count++;
            tempDictionary.put(key, value);

            if (key.length() > maxlength) {
                maxlength = key.length();
            }
        }

        /*
         * edits/suggestions are created only once, no matter how often word occurs
         *
         * edits/suggestions are created only as soon as the word occurs in the corpus,
         *
         * even if the same term existed before in the tempDictionary as an edit from // another word
         *
         * a treshold might be specifid, when a term occurs so frequently in the corpus that it is considered a valid word for spelling correction
         */
        if (value.count == 1) {
            // word2index
            size++;
            // int keyint = tempWordlist.size() - 1;

            result = true;
            final int maxEditDist = this.restricetdEditDistanceMax;
            // create deletes
            for (String delete : edits(key, maxEditDist)) {
                Object value2 = tempDictionary.get(delete);
                if (value2 != null) {
                    // already exists:
                    // 1. word1==deletes(word2)
                    // 2. deletes(word1)==deletes(word2)
                    // int or dictionaryItem? single delete existed before!
                    if (value2 instanceof String) {
                        // transformes int to dictionaryItem
                        String tmp = String.class.cast(value2);
                        Node di = new Node();
                        di.suggestions.add(tmp);
                        tempDictionary.put(delete, di);
                        if (!di.suggestions.contains(key)) {
                            addLowestDistance(di, key, delete);
                        }
                    } else if (!Node.class.cast(value2).suggestions.contains(key)) {
                        addLowestDistance(Node.class.cast(value2), key, delete);
                    }
                } else {
                    tempDictionary.put(delete, key);
                }

            }
        }
        return result;
    }

    // save some time and space
    private void addLowestDistance(Node item, String suggestion, String delete) {
        // // remove all existing suggestions of higher distance, if verbose<2 index2word
        // // TODO check
        if ((verbose < 2) && (item.suggestions.size() > 0) && ((item.suggestions.get(0).length() - delete.length()) > (suggestion.length() - delete.length()))) {
            item.suggestions.clear();
        }
        // do not add suggestion of higher distance than existing, if verbose<2
        if ((verbose == 2) || (item.suggestions.size() == 0) || ((item.suggestions.get(0).length() - delete.length()) >= (suggestion.length() - delete.length()))) {
            item.suggestions.add(suggestion);
        }
        item.suggestions.add(suggestion);
    }

    @Override
    public int getMaxLength() {
        return maxlength;
    }

    @Override
    public DictionaryItem getEntry(String candidate) {
        // read candidate entry from tempDictionary
        Object dictionaryEntry = this.dictionary.get(candidate);

        if (dictionaryEntry != null) {
            if (dictionaryEntry instanceof String) {
                DictionaryItem matchedDictionaryItem = new DictionaryItem();
                matchedDictionaryItem.suggestions = new String[] { String.class.cast(dictionaryEntry) };
                return matchedDictionaryItem;
            } else {
                return DictionaryItem.class.cast(dictionaryEntry);
            }
        }
        return null;
    }

    @Override
    public void commit() {
        dictionary = new HashMap<>();

        this.tempDictionary.forEach((key, value) -> {
            if (value instanceof String) {
                dictionary.put(key, value);
            } else {
                Node itm = Node.class.cast(value);
                DictionaryItem i = new DictionaryItem();
                i.count = itm.count;
                i.suggestions = itm.suggestions.toArray(new String[itm.suggestions.size()]);
                dictionary.put(key, i);
                itm.suggestions = null;
            }
        });
        tempDictionary = null;
    }

    @Override
    public int getWordCount() {
        return size;// words.length;
    }

    @Override
    public int getEntryCount() {
        return dictionary.size();
    }

    @Override
    public IDictionaryItems getIterable() {
        // TODO Auto-generated method stub
        return null;
    }
}
