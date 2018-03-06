package com.faroo.symspell.impl.v3;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashSet;
import java.util.Set;

import com.faroo.symspell.ISymSpellIndex;
import com.faroo.symspell.Verbosity;

public interface IWordIndex extends ISymSpellIndex<SymSpellV3> {

    /**
     * Add the word to the dictionary
     * 
     * @param word
     * @return
     */
    default boolean addWord(String word) {
        return addWord(word, 1);
    }

    int getMaxLength();

    IMatchingItemsIterator getIterable();

    IMatchingItemsIterator getMatches(String candidate, IMatchingItemsIterator item);

    void commit();

    int getWordCount();

    int getEntryCount();

    int getDistance();
    
    Verbosity getVerbosity();

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
        if (editDistance <= editDistanceMax) {
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
        }
        return deletes;
    }

    default void commitTo(SymSpellV3 engine) {
        this.commit();
    }

    static int dist(int l, double k) {
        return (int) Math.round((1d - k) * l);
    }

    default int dist(String in) {
        return dist(in.length(), 0.65);
    }
    
    
    void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
    void writeExternal(final ObjectOutput out) throws IOException;
}