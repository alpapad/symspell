package com.faroo.symspell.impl.v6;
/**
 * Copyright (C) 2017 Wolf Garbe
 *
 * Version: 6.0
 * Author: Wolf Garbe <wolf.garbe@faroo.com>
 * Maintainer: Wolf Garbe <wolf.garbe@faroo.com>
 * URL: https://github.com/wolfgarbe/symspell
 *
 * Description: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.faroo.symspell.ISymSpellIndex;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * An intentionally opacque class used to temporarily stage dictionary data during the adding of many words. By staging the data during the building of the dictionary data, significant savings of time can be achieved, as well as a reduction in final memory usage.
 *
 */
public class SuggestionStage implements ISymSpellIndex<SymSpellV6> {

    private static class Node {
        public String suggestion;
        public int next;

        public Node(String suggestion, int next) {
            super();
            this.suggestion = suggestion;
            this.next = next;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((suggestion == null) ? 0 : suggestion.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Node other = (Node) obj;
            if (suggestion == null) {
                if (other.suggestion != null) {
                    return false;
                }
            } else if (!suggestion.equals(other.suggestion)) {
                return false;
            }
            return true;
        }
    }

    private static class Entry {
        public int first;
        public int count;

        public Entry(int count, int first) {
            super();
            this.first = first;
            this.count = count;
        }
    }

    private HashMap<Long, Entry> deletes;
    private ChunkArray<Node> nodes;

    private long compactMask;
    private int maxDictionaryEditDistance;
    private int prefixLength; // prefix length 5..7

    private int maxLength; // maximum dictionary term length

    /**
     * Create a new instance of SuggestionStage.
     * 
     * 
     * Specifying an accurate initialCapacity is not essential, but it can help speed up processing by aleviating the need for data restructuring as the size grows.
     * 
     * 
     * @param initialCapacity
     *            The expected number of words that will be added.
     */

    public SuggestionStage(int initialCapacity) {
        this.deletes = new HashMap<>(initialCapacity);
        this.nodes = new ChunkArray<>(initialCapacity * 2);
    }

    /** <summary>Gets the count of unique delete words. */
    public int getDeleteCount() {
        return deletes.size();
    }

    /** Gets the total count of all suggestions for all deletes. */
    public int getNodeCount() {
        return nodes.getCount();
    }

    /** Clears all the data from the SuggestionStaging. */
    public void clear() {
        deletes.clear();
        nodes.clear();
    }

    /**
     *
     * @param key
     * @return
     */
    private Set<String> editsPrefix(String key) {
        Set<String> hashSet = new LinkedHashSet<>();
        if (key.length() <= maxDictionaryEditDistance) {
            hashSet.add("");
        }
        if (key.length() > prefixLength) {
            key = key.substring(0, prefixLength);
        }
        hashSet.add(key);
        return edits(key, 0, hashSet);
    }

    /**
     * inexpensive and language independent: only deletes, no transposes + replaces + inserts replaces and inserts are expensive and language dependent (Chinese has 70,000 Unicode Han characters)
     *
     * @param word
     * @param editDistance
     * @param deleteWords
     * @return
     */
    private Set<String> edits(String word, int editDistance, Set<String> deleteWords) {
        editDistance++;
        if (word.length() > 1) {
            for (int i = 0; i < word.length(); i++) {
                // String delete = word.Remove(i, 1);
                String delete = word.substring(0, i) + word.substring(i + 1);
                if (deleteWords.add(delete)) {
                    // recursion, if maximum edit distance not yet reached
                    if (editDistance < maxDictionaryEditDistance) {
                        edits(delete, editDistance, deleteWords);
                    }
                }
            }
        }
        return deleteWords;
    }

    /**
     * 
     * @param deleteHash
     * @param suggestion
     */
    void add(long deleteHash, String suggestion) {
        Entry entry = deletes.get(deleteHash);
        if (entry == null) {
            entry = new Entry(0, -1);
        }
        int next = entry.first;
        entry.count++;
        entry.first = nodes.getCount();
        deletes.put(deleteHash, entry);
        nodes.add(new Node(suggestion, next));
    }

    /**
     * 
     * @param permanentDeletes
     */
    void commitTo(Long2ObjectOpenHashMap<String[]> permanentDeletes) {
        for (Map.Entry<Long, Entry> keyPair : deletes.entrySet()) {
            int i;
            String[] suggestions = permanentDeletes.get(keyPair.getKey().longValue());
            if (suggestions != null) {
                i = suggestions.length;
                String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + keyPair.getValue().count);
                permanentDeletes.put(keyPair.getKey().longValue(), newSuggestions);
                suggestions = newSuggestions;
            } else {
                i = 0;
                suggestions = new String[keyPair.getValue().count];
                permanentDeletes.put(keyPair.getKey().longValue(), suggestions);
            }
            int next = keyPair.getValue().first;
            while (next >= 0) {
                SuggestionStage.Node node = nodes.get(next);
                suggestions[i] = node.suggestion;
                next = node.next;
                i++;
            }
        }
    }

    @Override
    public boolean addWord(String key, long count) {
        // edits/suggestions are created only once, no matter how often word occurs
        // edits/suggestions are created only as soon as the word occurs in the corpus,
        // even if the same term existed before in the dictionary as an edit from
        // another word
        if (key.length() > maxLength) {
            maxLength = key.length();
        }
        // create deletes
        Set<String> edits = editsPrefix(key);
        // if not staging suggestions, put directly into main data structure
        for (String delete : edits) {
            add(getStringHash(delete), key);
        }
        return false;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void commitTo(SymSpellV6 engine) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEntryCount() {
        return nodes.getCount();
    }

    /**
     *
     * @param s
     * @return
     */
    private long getStringHash(String s) {
        int len = s.length();
        int lenMask = len;
        if (lenMask > 3) {
            lenMask = 3;
        }

        long hash = 2166136261l;
        for (int i = 0; i < len; i++) {
            // unchecked
            {
                hash ^= s.charAt(i);
                hash *= 16777619;
            }
        }

        hash &= this.compactMask;
        hash |= lenMask;
        return hash;
    }
}