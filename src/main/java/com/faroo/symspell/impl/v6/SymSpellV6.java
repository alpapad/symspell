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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.faroo.symspell.ISymSpell;
import com.faroo.symspell.SuggestItem;
import com.faroo.symspell.Verbosity;
import com.google.common.base.Stopwatch;
import com.google.common.math.StatsAccumulator;

import gnu.trove.impl.Constants;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class SymSpellV6 implements ISymSpell {

    static final int defaultMaxEditDistance = 2;
    static final int defaultPrefixLength = 137;
    static final int defaultCountThreshold = 0;
    static final int defaultInitialCapacity = 16;
    static final int defaultCompactLevel = 5;

    private int initialCapacity;
    private int maxDictionaryEditDistance;
    private int prefixLength; // prefix length 5..7
    private long countThreshold; // a treshold might be specifid, when a term occurs so frequently in the corpus
                                 // that it is considered a valid word for spelling correction
    private long compactMask;
    private EditDistance.DistanceAlgorithm distanceAlgorithm = EditDistance.DistanceAlgorithm.Damerau;
    private int maxLength; // maximum dictionary term length

    private final static long NO_ENTRY = Long.MIN_VALUE;
    /*
     * HashMapDictionary that contains a mapping of lists of suggested correction words to the hashCodes of the original words and the deletes derived from them. Collisions of hashCodes is tolerated, because suggestions are ultimately verified via an edit distance function. A list of suggestions might have a single suggestion, or multiple suggestions.
     */
    private TLongObjectMap<String[]> deletes;
    /*
     * HashMapDictionary of unique correct spelling words, and the frequency count for each word.
     */
    private TObjectLongMap<String> words;
    /*
     * HashMapDictionary of unique words that are below the count threshold for being considered correct spellings.
     */
    private TObjectLongMap<String> belowThresholdWords = new TObjectLongHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY);

    /* Maximum edit distance for dictionary precalculation. */
    public int getMaxDictionaryEditDistance() {
        return this.maxDictionaryEditDistance;
    }

    /** Length of prefix, from which deletes are generated. */
    public int getPrefixLength() {
        return this.prefixLength;
    }

    /** Length of longest word in the dictionary. */
    public int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Count threshold for a word to be considered a valid word for spelling correction.
     */
    public long getCountThreshold() {
        return this.countThreshold;
    }

    /** Number of unique words in the dictionary. */
    public int getWordCount() {
        return this.words.size();
    }

    public final StatsAccumulator acc = new StatsAccumulator();
    public final StatsAccumulator du = new StatsAccumulator();
    private Stopwatch stopWatch = Stopwatch.createUnstarted();

    /**
     * Number of word prefixes and intermediate word deletes encoded in the dictionary.
     */
    public int getEntryCount() {
        return this.deletes.size();
    }

    public SymSpellV6(int distance) {
        this(defaultInitialCapacity, distance, defaultPrefixLength, defaultCountThreshold, (byte) defaultCompactLevel);
    }

    public SymSpellV6() {
        this(defaultInitialCapacity, defaultMaxEditDistance, defaultPrefixLength, defaultCountThreshold, (byte) defaultCompactLevel);
    }

    /**
     * Create a new instanc of SymSpellV3.
     * 
     * Specifying ann accurate initialCapacity is not essential, but it can help speed up processing by aleviating the need for data restructuring as the size grows.
     * 
     * 
     * @param initialCapacity
     *            The expected number of words in dictionary.
     * @param maxDictionaryEditDistance
     *            Maximum edit distance for doing lookups.
     * @param prefixLength
     *            The length of word prefixes used for spell checking..
     * @param countThreshold
     *            The minimum frequency count for dictionary words to be considered correct spellings.
     * @param compactLevel
     *            Degree of favoring lower memory use over speed (0=fastest,most memory, 16=slowest,least memory).
     *
     */
    public SymSpellV6(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, int countThreshold, byte compactLevel) {
        if (initialCapacity < 0) {
            throw new IndexOutOfBoundsException("initialCapacity");
        }
        if (maxDictionaryEditDistance < 0) {
            throw new IndexOutOfBoundsException("maxDictionaryEditDistance");
        }
        if ((prefixLength < 1) || (prefixLength <= maxDictionaryEditDistance)) {
            throw new IndexOutOfBoundsException("prefixLength");
        }
        if (countThreshold < 0) {
            throw new IndexOutOfBoundsException("countThreshold");
        }
        if (compactLevel > 16) {
            throw new IndexOutOfBoundsException("compactLevel");
        }

        this.initialCapacity = initialCapacity;
        this.words = new TObjectLongHashMap<String>(initialCapacity, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY);
        this.maxDictionaryEditDistance = maxDictionaryEditDistance;
        this.prefixLength = prefixLength;
        this.countThreshold = countThreshold;
        if (compactLevel > 16) {
            compactLevel = 16;
        }
        this.compactMask = (0xFFFFFFFF >> (3 + compactLevel)) << 2;
    }

    /**
     * Create/Update an entry in the dictionary.
     * 
     * 
     * For every word there are deletes with an edit distance of 1..maxEditDistance created and added to the dictionary. Every delete entry has a suggestions list, which points to the original term(s) it was created from. The dictionary may be dynamically updated (word frequency and new words) at any time by calling CreateDictionaryEntry
     * 
     * @param key
     *            The word to add to dictionary.
     * @param count
     *            The frequency count for word.
     * @param staging
     *            Optional staging object to speed up adding many entries by staging them to a temporary structure.
     * @return True if the word was added as a new correctly spelled word, or false if the word is added as a below threshold word, or updates an existing correctly spelled word.
     */
    public boolean createDictionaryEntry(String key, Long count, SuggestionStage staging) {

        if (count <= 0) {
            if (this.countThreshold > 0) {
                return false; // no point doing anything if count is zero, as it can't change anything
            }
            count = 0l;
        }
        Long countPrevious = -1l;

        // look first in below threshold words, update count, and allow promotion to
        // correct spelling word if count reaches threshold
        // threshold must be >1 for there to be the possibility of low threshold words
        if ((countThreshold > 1) && ((countPrevious = belowThresholdWords.get(key)) != belowThresholdWords.getNoEntryValue())) {
            // calculate new count for below threshold word
            count = ((Long.MAX_VALUE - countPrevious) > count) ? countPrevious + count : Long.MAX_VALUE;
            // has reached threshold - remove from below threshold collection (it will be
            // added to correct words below)
            if (count >= countThreshold) {
                belowThresholdWords.remove(key);
            } else {
                belowThresholdWords.put(key, count);
                return false;
            }
        } else if ((countPrevious = words.get(key)) != words.getNoEntryValue()) {
            // just update count if it's an already added above threshold word
            count = ((Long.MAX_VALUE - countPrevious) > count) ? countPrevious + count : Long.MAX_VALUE;
            words.put(key, count);
            return false;
        } else if (count < getCountThreshold()) {
            // new or existing below threshold word
            belowThresholdWords.put(key, count);
            return false;
        }

        // what we have at this point is a new, above threshold word
        words.put(key, count);

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
        if (staging != null) {
            for (String delete : edits) {
                staging.add(getStringHash(delete), key);
            }
        } else {
            if (deletes == null) {
                this.deletes = new TLongObjectHashMap<>(initialCapacity); // initialisierung
            }
            for (String delete : edits) {
                long deleteHash = getStringHash(delete);
                String[] suggestions = deletes.get(deleteHash);

                if (suggestions != null) {
                    // String[] newSuggestions = new String[suggestions.length + 1];
                    String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + 1);
                    // copy(suggestions, newSuggestions, suggestions.length);
                    deletes.put(deleteHash, newSuggestions);
                    suggestions = newSuggestions;
                } else {
                    suggestions = new String[1];
                    deletes.put(deleteHash, suggestions);
                }
                suggestions[suggestions.length - 1] = key;
            }
        }
        return true;
    }

    /**
     * Load multiple dictionary entries from a file of word/frequency count pairs
     * 
     * Merges with any dictionary data already loaded.
     * 
     * @param corpus
     *            The path+filename of the file.
     * @param termIndex
     *            The column position of the word.
     * @param countIndex
     *            The column position of the frequency count.
     * @return True if file loaded, or false if file not found.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean loadDictionary(String corpus, int termIndex, int countIndex) throws FileNotFoundException, IOException {
        if (!Files.exists(Paths.get(corpus))) {
            return false;
        }
        SuggestionStage staging = new SuggestionStage(16384);
        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length >= 2) {
                    String key = lineParts[termIndex];
                    try {
                        Long count = Long.valueOf(lineParts[countIndex]);
                        createDictionaryEntry(key, count, staging);
                    } catch (Exception e) {

                    }
                }
            }
        }

        if (this.deletes == null) {
            this.deletes = new TLongObjectHashMap<>(staging.getDeleteCount());
        }
        commitStaged(staging);
        return true;
    }

    /**
     * Create a frequency dictionary from a corpus (merges with any dictionary data already loaded)
     * 
     * Load multiple dictionary words from a file containing plain text.
     * 
     * @param corpus
     *            The path+filename of the file.
     * @return True if file loaded, or false if file not found.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean createDictionary(String corpus) throws FileNotFoundException, IOException {
        if (!Files.exists(Paths.get(corpus))) {
            return false;
        }
        SuggestionStage staging = new SuggestionStage(16384);
        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            // process a single line at a time only for memory efficiency
            while ((line = br.readLine()) != null) {
                // for (String key : parseWords(line)) {
                createDictionaryEntry(line.toLowerCase().trim(), 1l, staging);
                // }
            }
        }

        if (this.deletes == null) {
            this.deletes = new TLongObjectHashMap<>(staging.getDeleteCount());
        }
        commitStaged(staging);
        return true;
    }

    public boolean commit(SuggestionStage staging) {
        if (this.deletes == null) {
            this.deletes = new TLongObjectHashMap<>(staging.getDeleteCount());
        }
        commitStaged(staging);
        return true;
    }

    /**
     * Remove all below threshold words from the dictionary.
     * 
     * This can be used to reduce memory consumption after populating the dictionary from a corpus using CreateDictionary.
     */
    public void purgeBelowThresholdWords() {
        belowThresholdWords = new TObjectLongHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY);
    }

    /**
     * Commit staged dictionary additions.
     * 
     * Used when you write your own process to load multiple words into the dictionary, and as part of that process, you first created a SuggestionsStage object, and passed that to CreateDictionaryEntry calls.
     * 
     * @param staging
     *            The SuggestionStage object storing the staged data.
     */
    private void commitStaged(SuggestionStage staging) {
        staging.commitTo(deletes);
    }

    /**
     * Find suggested spellings for a given input word, using the maximum edit distance specified during construction of the SymSpellV3 dictionary.
     * 
     * @param input
     *            The word being spell checked.
     * @param verbose
     *            The value controlling the quantity/closeness of the retuned suggestions.
     * @return A List of SuggestItem object representing suggested correct spellings for the input word, sorted by edit distance, and secondarily by count frequency.
     */
    public List<SuggestItem> lookup(String input, Verbosity verbosity) {
        return lookup(input, verbosity, this.maxDictionaryEditDistance);
    }

    @Override
    public List<SuggestItem> lookup(String input, int editDistanceMax) {
        return lookup(input, Verbosity.All, editDistanceMax);
    }

    /**
     * Find suggested spellings for a given input word.
     * 
     * @param input
     *            The word being spell checked.
     * @param verbose
     *            The value controlling the quantity/closeness of the retuned suggestions.
     * @param maxEditDistance
     *            The maximum edit distance between input and suggested words.
     * @return A List of SuggestItem object representing suggested correct spellings for the input word, sorted by edit distance, and secondarily by count frequency.
     */
    public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) {
        /*
         * verbose=Top: the suggestion with the highest term frequency of the suggestions of smallest edit distance found
         * 
         * verbose=Closest: all suggestions of smallest edit distance found, the suggestions are ordered by term frequency
         * 
         * verbose=All: all suggestions <= maxEditDistance, the suggestions are ordered by edit distance, then by term frequency (slower, no early termination)
         */

        /*
         * maxEditDistance used in Lookup can't be bigger than the maxDictionaryEditDistance
         * 
         * used to construct the underlying dictionary structure.
         */
        if (maxEditDistance > getMaxDictionaryEditDistance()) {
            throw new IndexOutOfBoundsException("maxEditDistance can not be higher than " + getMaxDictionaryEditDistance());
        }

        List<SuggestItem> suggestions = new ArrayList<SuggestItem>();
        final int inputLen = input.length();
        // early exit - word is too big to possibly match any words
        if ((inputLen - maxEditDistance) > maxLength) {
            return suggestions;
        }

        // deletes we've considered already
        final Set<String> consideredDeletes = new HashSet<String>();
        // suggestions we've considered already
        final Set<String> consideredSuggestions = new HashSet<String>();

        // quick look for exact match
        long suggestionCount = words.get(input);
        if (suggestionCount != NO_ENTRY) {
            suggestions.add(new SuggestItem(input, 0, suggestionCount));
            // early exit - return exact match, unless caller wants all matches
            if (verbosity != Verbosity.All) {
                return suggestions;
            }
        } else {
            suggestionCount = 0l;
        }
        consideredSuggestions.add(input); // we considered the input already in the word.get() above

        int maxEditDistance2 = maxEditDistance;

        List<String> candidates = new ArrayList<>();

        // add original prefix
        int inputPrefixLen = inputLen;
        if (inputPrefixLen > prefixLength) {
            inputPrefixLen = prefixLength;
            candidates.add(input.substring(0, inputPrefixLen));
        } else {
            candidates.add(input);
        }

        EditDistance distanceComparer = new EditDistance(input, this.distanceAlgorithm);
        int candidatePointer = 0;

        while (candidatePointer < candidates.size()) {

            String candidate = candidates.get(candidatePointer++);
            int candidateLen = candidate.length();
            int lengthDiff = inputPrefixLen - candidateLen;

            /*
             * save some time - early termination
             * 
             * if canddate distance is already higher than suggestion distance, then there are no better suggestions to be expected
             */
            if (lengthDiff > maxEditDistance2) {
                /*
                 * skip to next candidate if Verbosity.All, look no further if Verbosity.Top or Closest (candidates are ordered by delete distance, so none are closer than current)
                 */
                if (verbosity == Verbosity.All) {
                    continue;
                }
                break;
            }

            // read candidate entry from dictionary
            String[] dictSuggestions = deletes.get(getStringHash(candidate));

            if (dictSuggestions != null) {
                // iterate through suggestions (to other correct dictionary items) of delete
                // item and add them to suggestion list
                for (int i = 0; i < dictSuggestions.length; i++) {
                    String suggestion = dictSuggestions[i];
                    int suggestionLen = suggestion.length();
                    if (suggestion.equals(input)) {
                        // System.err.println("suggestionStr.equals(inputStr)");
                        continue;
                    }
                    if ((Math.abs(suggestionLen - inputLen) > maxEditDistance2) // input and sugg lengths diff >
                                                                                // allowed/current best distance
                            || (suggestionLen < candidateLen) // sugg must be for a different delete string, in same bin // only because of hash collision
                            || ((suggestionLen == candidateLen) && (!suggestion.equals(candidate)))) {
                        // System.err.println("bla1 .equals(inputStr)");
                        continue;
                    }
                    int suggPrefixLen = Math.min(suggestionLen, prefixLength);
                    if ((suggPrefixLen > inputPrefixLen) && ((suggPrefixLen - candidateLen) > maxEditDistance2)) {
                        // System.err.println("bla2 .equals(inputStr)");
                        continue;
                    }

                    /*
                     * True Damerau-Levenshtein Edit Distance: adjust distance, if both distances>0 We allow simultaneous edits (deletes) of maxEditDistance on both the dictionary and the input term.
                     * 
                     * For replaces and adjacent transposes the resulting edit distance stays <= maxEditDistance.
                     * 
                     * For inserts and deletes the resulting edit distance might exceed maxEditDistance.
                     */

                    /*
                     * To prevent suggestions of a higher edit distance, we need to calculate the resulting edit distance, if there are simultaneous edits on both sides. Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and bank!=baxn for maxEditDistance=1)
                     * 
                     * Two deletes on each side of a pair makes them all equal, but the first two pairs have edit distance=1, the others edit distance=2.
                     */
                    int distance = 0;
                    int min = 0;
                    if (candidateLen == 0) {
                        /*
                         * suggestions which have no common chars with input (inputLen<=maxEditDistance && suggestionLen<=maxEditDistance)
                         */
                        distance = Math.max(inputLen, suggestionLen);
                        if ((distance > maxEditDistance2) || !consideredSuggestions.add(suggestion)) {
                            continue;
                        }
                    } else if (suggestionLen == 1) {
                        if (input.indexOf(suggestion.charAt(0)) < 0) {
                            distance = inputLen;
                        } else {
                            distance = inputLen - 1;
                        }
                        if ((distance > maxEditDistance2) || !consideredSuggestions.add(suggestion)) {
                            continue;
                        }
                    } else
                    /*
                     * number of edits in prefix ==maxediddistance AND no identic suffix , then editdistance>maxEditDistance and no need for Levenshtein calculation (inputLen >= prefixLength) && (suggestionLen >= prefixLength)
                     */
                    if (//
                    (//
                    ((prefixLength - maxEditDistance) == candidateLen)//
                            && ((min = Math.min(inputLen, suggestionLen) - prefixLength) > 1) //
                            && (!input.substring((inputLen + 1) - min).equals(suggestion.substring((suggestionLen + 1) - min)))//
                    ) || //
                            (//
                            (min > 0)//
                                    && (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min))//
                                    && (//
                                    (input.charAt(inputLen - min - 1) != suggestion.charAt(suggestionLen - min))//
                                            || //
                                            (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min - 1))//
                                    ))//
                    ) {
                        continue;
                    } else {
                        /* deleteInSuggestionPrefix is somewhat expensive, and only pays off when verbose is Top or Closest. */
                        if (((!Verbosity.All.equals(verbosity)) && !deleteInSuggestionPrefix(candidate, candidateLen, suggestion, suggestionLen)) || !consideredSuggestions.add(suggestion)) {
                            continue;
                        }
                        stopWatch.reset().start();
                        distance = distanceComparer.Compare(suggestion, maxEditDistance2);
                        du.add(stopWatch.stop().elapsed().getNano());
                        acc.add(1d);
                        if (distance < 0) {
                            continue;
                        }
                    }

                    // save some time
                    // do not process higher distances than those already found, if verbose<All
                    // (note: maxEditDistance2 will always equal maxEditDistance when Verbosity.All)
                    if (distance <= maxEditDistance2) {
                        suggestionCount = words.get(suggestion);
                        SuggestItem si = new SuggestItem(suggestion, distance, suggestionCount);
                        if (suggestions.size() > 0) {
                            switch (verbosity) {
                            case Closest: {
                                // we will calculate DamLev distance only to the smallest found distance so far
                                if (distance < maxEditDistance2) {
                                    suggestions.clear();
                                }
                                break;
                            }
                            case Top: {
                                if ((distance < maxEditDistance2) || (suggestionCount > suggestions.get(0).count)) {
                                    maxEditDistance2 = distance;
                                    suggestions.add(0, si);
                                }
                                continue;
                            }
                            default:
                            }
                        }
                        if (verbosity != Verbosity.All) {
                            maxEditDistance2 = distance;
                        }
                        suggestions.add(si);
                    }
                } // end foreach
            } // end if

            // add edits
            // derive edits (deletes) from candidate (input) and add them to candidates list
            // this is a recursive process until the maximum edit distance has been reached
            if ((lengthDiff < maxEditDistance) && (candidateLen <= prefixLength)) {
                // save some time
                // do not create edits with edit distance smaller than suggestions already found
                if ((!Verbosity.All.equals(verbosity)) && (lengthDiff >= maxEditDistance2)) {
                    continue;
                }

                for (int i = 0; i < candidateLen; i++) {
                    // String delete = candidate.Remove(i, 1);
                    String delete = candidate.substring(0, i) + candidate.substring(i + 1);
                    if (consideredDeletes.add(delete)) {
                        candidates.add(delete);
                    }
                }
            }
        } // end while

        // sort by ascending edit distance, then by descending word frequency
        if (suggestions.size() > 1) {
            suggestions.sort((o1, o2) -> o1.compareTo(o2));
        }
        return suggestions;
    }// end if

    /**
     * Check whether all delete chars are present in the suggestion prefix in correct order, otherwise this is just a hash collision
     * 
     * @param delete
     * @param deleteLen
     * @param suggestion
     * @param suggestionLen
     * @return
     */
    private boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion, int suggestionLen) {
        if (deleteLen == 0) {
            return true;
        }
        if (prefixLength < suggestionLen) {
            suggestionLen = prefixLength;
        }
        int j = 0;
        for (int i = 0; i < deleteLen; i++) {
            char delChar = delete.charAt(i);
            while ((j < suggestionLen) && (delChar != suggestion.charAt(j))) {
                j++;
            }
            if (j == suggestionLen) {
                return false;
            }
        }
        return true;
    }

    // //create a non-unique wordlist from sample text
    // //language independent (e.g. works with Chinese characters)
    // private String[] ParseWords(String text)
    // {
    // // \w Alphanumeric characters (including non-latin characters, umlaut
    // characters and digits) plus "_"
    // // \d Digits
    // // Compatible with non-latin characters, does not split words at apostrophes
    // MatchCollection mc = Regex.Matches(text.ToLower(), @"['’\w-[_]]+");
    //
    // //for benchmarking only: with CreateDictionary("big.txt","") and the text
    // corpus from http://norvig.com/big.txt the Regex below provides the exact same
    // number of dictionary items as Norvigs regex "[a-z]+" (which splits words at
    // apostrophes & incompatible with non-latin characters)
    // //MatchCollection mc = Regex.Matches(text.ToLower(), @"[\w-[\d_]]+");
    //
    // String[] matches = new String[mc.Count];
    // for (int i = 0; i < matches.Length; i++) matches[i] = mc[i].ToString();
    // return matches;
    // }

    static final Pattern sp = Pattern.compile("[\\w-[\\d_]]+");

    /**
     * create a non-unique wordlist from sample text language independent (e.g. works with Chinese characters)
     * 
     * @param text
     * @return
     */
    public static Iterable<String> parseWords(String text) {
        /*
         * \w Alphanumeric characters (including non-latin characters, umlaut characters and digits) plus "_" \d Digits
         * 
         * Provides identical results to Norvigs regex "[a-z]+" for latin characters, while additionally providing compatibility with non-latin characters
         */
        List<String> allMatches = new ArrayList<String>();
        Matcher m = sp.matcher(text.toLowerCase());
        if (m.matches()) {
            while (m.find()) {
                allMatches.add(m.group());
            }
        } else {
            allMatches.add(text.toLowerCase());
        }
        return allMatches;
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
     * @param key
     * @return
     */
    private Set<String> editsPrefix(String key) {
        Set<String> hashSet = new LinkedHashSet<String>();
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
