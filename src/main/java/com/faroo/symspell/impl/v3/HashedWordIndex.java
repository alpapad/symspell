/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell.impl.v3;

import static com.faroo.symspell.hash.XxHash64.hash;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.faroo.symspell.Verbosity;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

/**
 * HashMapDictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
 */
public class HashedWordIndex implements IWordIndex {
    private TLongObjectMap<Object> tempDictionary = new TLongObjectHashMap<>();

    /**
     * HashMapDictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
     *
     * Instead of keeping the words as pointers, we use a very low collision hash of the word as a key.
     *
     * For space reduction a item might be either of type of the values can be either an integer or a DictionaryItem
     *
     * A DictionaryItem is used for word, word/delete, and delete with multiple suggestions. integer is used for deletes with a single suggestion (the majority of entries).
     */
    private TLongObjectMap<Object> dictionary = null;

    private int wordCount = 0;

    private int verbose = 2;
    private int restricetdEditDistanceMax = 2;

    public int maxlength = 0;// maximum tempDictionary term length

    private static class Node {
        public List<String> suggestions = new ArrayList<>();
        public int count = 0;
    }

    private final Verbosity verbosity;
    public HashedWordIndex(int editDistanceMax, Verbosity verbosity) {
        super();
        this.restricetdEditDistanceMax = editDistanceMax;
        this.verbosity = verbosity;
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
    public boolean addWord(String key, long count) {
        boolean result = false;
        Node node = null;
        Object valueo = tempDictionary.get(hash(key));
        if (valueo != null) {
            // int or dictionaryItem? delete existed before word!
            if (valueo instanceof String) {
                String tmp = String.class.cast(valueo);
                node = new Node();
                node.suggestions.add(tmp);
                tempDictionary.put(hash(key), node);
            } else {
                // already exists:
                // 1. word appears several times
                // 2. word1==deletes(word2)
            	node = Node.class.cast(valueo);
            }

            // prevent overflow
            if (node.count < Integer.MAX_VALUE) {
            	node.count++;
            }
        } else {
        	node = new Node();
        	node.count++;
            tempDictionary.put(hash(key), node);

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
        if (node.count == 1) {
            wordCount++;
            result = true;
            final int maxEditDist = this.restricetdEditDistanceMax;
            // create deletes
            for (String delete : edits(key, maxEditDist)) {
                Object value2 = tempDictionary.get(hash(delete));
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
                        tempDictionary.put(hash(delete), di);
                        if (!di.suggestions.contains(key)) {
                            addLowestDistance(di, key, delete);
                        }
                    } else if (!Node.class.cast(value2).suggestions.contains(key)) {
                        addLowestDistance(Node.class.cast(value2), key, delete);
                    }
                } else {
                    tempDictionary.put(hash(delete), key);
                }

            }
        }
        return result;
    }

    // save some time and space
    private void addLowestDistance(Node item, String suggestion, String delete) {
        // remove all existing suggestions of higher distance, if verbose<2 index2word
        // TODO check
        if ((verbose < 2) && (item.suggestions.size() > 0) && ((item.suggestions.get(0).length() - delete.length()) > (suggestion.length() - delete.length()))) {
            item.suggestions.clear();
        }
        // do not add suggestion of higher distance than existing, if verbose<2
        if ((verbose == 2) || (item.suggestions.size() == 0) || ((item.suggestions.get(0).length() - delete.length()) >= (suggestion.length() - delete.length()))) {
            item.suggestions.add(suggestion);
        }
    }

    @Override
    public int getMaxLength() {
        return maxlength;
    }

    @Override
    public int getDistance() {
        return restricetdEditDistanceMax;
    }
    @Override
    public Verbosity getVerbosity() {
        return verbosity;
    }
    
    private DictionaryItem getEntry(String candidate) {
        // read candidate entry from tempDictionary
        Object dictionaryEntry = this.dictionary.get(hash(candidate));

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
    public IMatchingItemsIterator getMatches(String candidate, IMatchingItemsIterator item) {
        DictionaryItem itm = getEntry(candidate);
        if (itm != null) {
            return new DictionaryItemIterator(itm);
        }
        return null;
    }

    @Override
    public void commit() {
        dictionary = new TLongObjectHashMap<>();
        this.tempDictionary.forEachEntry(new TLongObjectProcedure<Object>() {
            @Override
            public boolean execute(long key, Object value) {
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
                return true;
            }
        });
        tempDictionary = null;
    }

    @Override
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public int getEntryCount() {
        return dictionary.size();
    }

    @Override
    public IMatchingItemsIterator getIterable() {
        // TODO Auto-generated method stub
        return null;
    }
    

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new IOException("Not supported!");
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new IOException("Not supported!");
    }
}
