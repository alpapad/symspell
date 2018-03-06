/**
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License,
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
package com.faroo.symspell.impl.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.faroo.symspell.ISymSpell;
import com.faroo.symspell.SuggestItem;
import com.faroo.symspell.Verbosity;
import com.faroo.symspell.distance.DistanceAlgo;
import com.faroo.symspell.distance.IDistance;
import com.faroo.symspell.string.StringToCharArr;
import com.google.common.base.Stopwatch;
import com.google.common.math.StatsAccumulator;

/**
 * SymSpellV3: 1 million times faster through Symmetric Delete spelling correction algorithm
 *
 * The Symmetric Delete spelling correction algorithm reduces the complexity of edit candidate generation and dictionary lookup for a given Damerau-Levenshtein distance. It is six orders of magnitude faster and language independent. Opposite to other algorithms only deletes are required, no transposes + replaces + inserts. Transposes + replaces + inserts of the input term are
 * transformed into deletes of the dictionary term. Replaces and inserts are expensive and language dependent: e.g. Chinese has 70,000 Unicode Han characters!
 *
 * Copyright (C) 2015 Wolf Garbe Version: 3.0 Author: Wolf Garbe <wolf.garbe@faroo.com> Maintainer: Wolf Garbe <wolf.garbe@faroo.com> URL: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/ Description: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
 */
public class SymSpellV3 implements ISymSpell {
    private static final int DEFAULT_EDIT_DISTANCE_MAX = 2;
    private static final Verbosity DEFAULT_VERBOSITY = Verbosity.All;
    private static final DistanceAlgo DEFAULT_DISTANCE_ALGO = DistanceAlgo.OptimalStringAlignment;
    private static final IndexAlgo DEFAULT_INDEX_ALGO = IndexAlgo.FastUtilCompact;
    /*
     * HashMapDictionary that contains both the original words and the deletes derived from them. A term might be both word and delete from another word at the same time.
     *
     * For space reduction a item might be either of type dictionaryItem or Int.
     *
     * A dictionaryItem is used for word, word/delete, and delete with multiple suggestions. Int is used for deletes with a single suggestion (the majority of entries).
     */
    private final IWordIndex dictionary;
    
    private final IDistance algo;
    
    public final int editDistanceMax;
    
    private final Verbosity verbosity;

    public final StatsAccumulator acc = new StatsAccumulator();
    public final StatsAccumulator du = new StatsAccumulator();
    private Stopwatch stopWatch = Stopwatch.createUnstarted();

    public SymSpellV3() {
        this(DEFAULT_EDIT_DISTANCE_MAX, DEFAULT_VERBOSITY, DEFAULT_DISTANCE_ALGO, DEFAULT_INDEX_ALGO);
    }

    public SymSpellV3(int editDistanceMax) {
        this(editDistanceMax, DEFAULT_VERBOSITY, DEFAULT_DISTANCE_ALGO, DEFAULT_INDEX_ALGO);
    }

    public SymSpellV3(int editDistanceMax, Verbosity verbosity) {
        this(editDistanceMax, verbosity, DEFAULT_DISTANCE_ALGO, DEFAULT_INDEX_ALGO);
    }

    public SymSpellV3(int editDistanceMax, Verbosity verbosity, DistanceAlgo algo) {
        this(editDistanceMax, verbosity, algo, DEFAULT_INDEX_ALGO);
    }

    public SymSpellV3(int editDistanceMax, Verbosity verbosity, DistanceAlgo algo, IndexAlgo dictionary) {
        this(editDistanceMax, verbosity, algo, dictionary.instance(editDistanceMax, verbosity));
    }

    public SymSpellV3(DistanceAlgo algo, IWordIndex dictionary) {
        this(dictionary.getDistance(), dictionary.getVerbosity(), algo, dictionary);
    }
    
    public SymSpellV3(DistanceAlgo algo, Verbosity verbosity, IWordIndex dictionary) {
        this(dictionary.getDistance(), verbosity,algo,  dictionary);
    }

    public SymSpellV3(int editDistanceMax, Verbosity verbosity, DistanceAlgo algo, IWordIndex dictionary) {
        super();
        this.editDistanceMax = editDistanceMax;
        this.verbosity = verbosity;
        this.dictionary = dictionary;;
        this.algo = algo.instance();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.faroo.symspell.ISymSpell#lookup(java.lang.String, int)
     */
    @Override
    public List<SuggestItem> lookup(String inputStr, int editDistanceMax) {
        return this.lookup(inputStr, verbosity, editDistanceMax);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.faroo.symspell.ISymSpell#lookup(java.lang.String, com.faroo.symspell.Verbosity, int)
     */
    @Override
    public List<SuggestItem> lookup(String inputStr, Verbosity verbosity, int editDistanceMax) {
        // final int editDistanceMax = Math.min(this.dictionary.dist(inputStr), editDistanceMax1);
        
        final int verbose = verbosity.verbose;
        // save some time
        if ((inputStr.length() - editDistanceMax) > dictionary.getMaxLength()) {
            return Collections.emptyList();
        }

        // final char[] input = inputStr.toCharArray();
        final int inputLen = inputStr.length();
        // final LinkedList<String> candidates = new LinkedList<String>();
        final List<String> candidates = new ArrayList<>();

        final Set<String> candidatesSeen = new HashSet<>();

        final List<SuggestItem> suggestions = new ArrayList<>();
        final Set<String> checkedWords = new HashSet<>();

        // add original term
        candidates.add(inputStr);
        final char[] inputArr = StringToCharArr.arr(inputStr);//.toCharArray();

        int candidatePointer = 0;
        final IMatchingItemsIterator iterator = dictionary.getIterable();

        while (candidatePointer < candidates.size()) {
            final String candidate = candidates.get(candidatePointer++);
            final int candidateLen = candidate.length();
            final int lengthDiff = Math.abs(inputLen - candidateLen);

            /*
             * save some time - early termination
             *
             * if canddate distance is already higher than suggestion distance, then there are no better suggestions to be expected
             */
            if (lengthDiff > editDistanceMax) {
                break;
            }

            // save some time
            // early termination
            // suggestion distance=candidate.distance... candidate.distance+editDistanceMax
            // if canddate distance is already higher than suggestion distance, than there
            // are no better suggestions to be expected

            // label for c# goto replacement
            nosort: {

                if ((verbose < 2) && (suggestions.size() > 0) && (lengthDiff > suggestions.get(0).distance)) {
                    break nosort;
                }

                // read candidate entry from dictionary
                final IMatchingItemsIterator entries = dictionary.getMatches(candidate, iterator);

                if (entries != null) {

                    // if count>0 then candidate entry is correct dictionary term, not only delete item
                    if (entries.isWord() && checkedWords.add(candidate)) {
                        // add correct dictionary term to suggestion list
                        suggestions.add(new SuggestItem(candidate, lengthDiff));
                        // early termination
                        if ((verbose < 2) && (lengthDiff == 0)) {
                            break nosort;
                        }
                    }

                    // iterate through suggestions (to other correct dictionary items) of delete
                    // item and add them to suggestion list
                    for (final String suggestionStr : entries) {
                        // save some time
                        // skipping double items early: different deletes of the input term can lead to
                        // the same suggestion

                        if (checkedWords.add(suggestionStr)) {
                            final int suggestionLen = suggestionStr.length();

                            // True Damerau-Levenshtein Edit Distance: adjust distance, if both distances>0
                            //
                            // We allow simultaneous edits (deletes) of editDistanceMax on both the dictionary and the input term.
                            // For replaces and adjacent transposes the resulting edit distance stays <= editDistanceMax.
                            //
                            // For inserts and deletes the resulting edit distance might exceed editDistanceMax.
                            // To prevent suggestions of a higher edit distance, we need to calculate the
                            // resulting edit distance, if there are simultaneous edits on both sides.
                            // Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and
                            // bank!=baxn for editDistanceMaxe=1)
                            // Two deletes on each side of a pair makes them all equal, but the first two
                            // pairs have edit distance=1, the others edit distance=2.
                            int distance = 0;
                            if (!suggestionStr.equals(inputStr)) {
                                if (suggestionLen == candidateLen) {
                                    distance = lengthDiff;
                                } else if (inputLen == candidateLen) {
                                    distance = suggestionLen - candidateLen;
                                } else {
                                    acc.add(1d);
                                    stopWatch.reset().start();
                                    distance = algo.distance(suggestionStr, inputArr, editDistanceMax);
                                    du.add(stopWatch.stop().elapsed().getNano());
                                }
                            }

                            // save some time.
                            // remove all existing suggestions of higher distance, if verbose<2
                            if ((verbose < 2) && (suggestions.size() > 0) && (suggestions.get(0).distance > distance)) {
                                suggestions.clear();
                            }
                            // do not process higher distances than those already found, if verbose<2
                            if ((verbose < 2) && (suggestions.size() > 0) && (distance > suggestions.get(0).distance)) {
                                continue;
                            }

                            if (distance <= editDistanceMax) {
                                suggestions.add(new SuggestItem(suggestionStr, distance));
                            }
                        }
                    } // end foreach
                } // end if

                // add edits
                // derive edits (deletes) from candidate (input) and add them to candidates list
                // this is a recursive process until the maximum edit distance has been reached
                if (lengthDiff < editDistanceMax) {
                    // save some time
                    // do not create edits with edit distance smaller than suggestions already found
                    if ((verbose < 2) && (suggestions.size() > 0) && (lengthDiff >= suggestions.get(0).distance)) {
                        continue;
                    }

                    for (int i = 0; i < candidateLen; i++) {
                        String delete = candidate.substring(0, i) + candidate.substring(i + 1);
                        if (candidatesSeen.add(delete)) {
                            candidates.add(delete);
                        }
                    }
                }
            } // end label nosort
        } // end while

        Collections.sort(suggestions, (x, y) -> x.compareTo(y));
        if ((verbose == 0) && (suggestions.size() > 1)) {
            return suggestions.subList(0, 1);
        } else {
            return suggestions;
        }
    }

    public boolean addWord(String lowerCase) {
        return this.dictionary.addWord(lowerCase);
    }

    public void commit() {
        this.dictionary.commit();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.faroo.symspell.ISymSpell#getMaxlength()
     */
    @Override
    public int getMaxLength() {
        return dictionary.getMaxLength();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.faroo.symspell.ISymSpell#getWordCount()
     */
    @Override
    public int getWordCount() {
        return dictionary.getWordCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.faroo.symspell.ISymSpell#getEntryCount()
     */
    @Override
    public int getEntryCount() {
        return dictionary.getEntryCount();
    }
}
