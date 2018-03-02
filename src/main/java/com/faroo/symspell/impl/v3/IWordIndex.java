package com.faroo.symspell.impl.v3;

import java.util.LinkedHashSet;
import java.util.Set;

import com.faroo.symspell.ISymSpellIndex;

public interface IWordIndex extends ISymSpellIndex<SymSpellV3>{

    /**
     * For every word there all deletes with an edit distance of 1..editDistanceMax created and added to the tempDictionary every delete entry has a suggestions list, which points to the original term(s) it was created from
     *
     * The tempDictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry
     *
     * @param key
     * @return
     */
    boolean createDictionaryEntry(String key);

    int getMaxLength();

    IMatchingItemsIterator getIterable();

    DictionaryItem getEntry(String candidate);

    default IMatchingItemsIterator getMatches(String candidate, IMatchingItemsIterator item) {
        DictionaryItem itm = getEntry(candidate);
        if (itm != null) {
            return new DictionaryItemIterator(itm);
        }
        return null;
    }

    void commit();

    int getWordCount();

    int getEntryCount();

    /**
     * Inexpensive and language independent: only deletes, no transposes + replaces +inserts
     *
     * Replaces and inserts are expensive and language dependent (Chinese has 70,000 Unicode Han characters)
     *
     * @param word
     * @param editDistanceMax
     * @return
     */
    default Set<String> edits(String word, int editDistanceMax) {
        return this.edits(word, 0, new LinkedHashSet<String>(), editDistanceMax);
    }

    /**
     * Inexpensive and language independent: only deletes, no transposes + replaces +inserts
     *
     * Replaces and inserts are expensive and language dependent (Chinese has 70,000 Unicode Han characters)
     *
     * @param word
     * @param editDistance
     * @param deletes
     * @param editDistanceMax
     * @return
     */
    default Set<String> edits(String word, int editDistance, Set<String> deletes, int editDistanceMax) {
        editDistance++;
        if (word.length() > 1) {
            for (int i = 0; i < word.length(); i++) {
                // delete ith character
                String delete = word.substring(0, i) + word.substring(i + 1);
                if (deletes.add(delete.intern())) {
                    // recursion, if maximum edit distance not yet reached
                    if (editDistance < editDistanceMax) {
                        edits(delete, editDistance, deletes, editDistanceMax);
                    }
                }
            }
        }
        return deletes;
    }
    
    
    default void commitTo(SymSpellV3 engine) {
        this.commit();
    }
}