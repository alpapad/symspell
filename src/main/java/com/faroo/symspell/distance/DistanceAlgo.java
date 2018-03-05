package com.faroo.symspell.distance;

public enum DistanceAlgo {
    Fastenshtein,
    DamerauLevenshtein, //
    Levenshtein, //
    LimitedLevenshtein, //
    OptimalStringAlignment,//
    Sift4, //
    JaroWinkler;

    public IDistance instance() {
        switch (this) {
        case DamerauLevenshtein:
            return new DamerauLevenshteinDistance();
        case Levenshtein:
            return new LevenshteinDistance();
        case LimitedLevenshtein:
            return new LimitedLevenshteinDistance();
        case OptimalStringAlignment:
            return new OptimalStringAlignmentDistance();
        case Sift4:
            return new Sift4();
        case JaroWinkler:
            return new JaroWinklerDistance();
        case Fastenshtein:
            return new FastenshteinLevDistance();
        default:
            throw new UnsupportedOperationException();
        }
    }
}
