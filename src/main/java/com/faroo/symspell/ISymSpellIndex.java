package com.faroo.symspell;

public interface ISymSpellIndex<T extends ISymSpell> {


    /**
     * For every word there all deletes with an edit distance of 1..editDistanceMax created and added to the tempDictionary every delete entry has a suggestions list, which points to the original term(s) it was created from
     *
     * The tempDictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry
     *
     * @param key
     * @return
     */
    boolean addWord(String key, long count);

    int getMaxLength();
    
    void commitTo(T engine);
    
    int getEntryCount();
}
