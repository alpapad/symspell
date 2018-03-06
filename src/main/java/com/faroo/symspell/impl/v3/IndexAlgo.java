package com.faroo.symspell.impl.v3;

import com.faroo.symspell.Verbosity;

public enum IndexAlgo {
    CustomCompact,
    FastUtilCompact,
    FastIntUtilCompact,
    KolobokeCompact,
    TroveCompact,
    HashMap,
    HashedWord;
    
    public IWordIndex instance(int editDistanceMax, Verbosity verbosity) {
        switch (this) {
        case CustomCompact:
            return new CustomCompactWordIndex(editDistanceMax, verbosity);
        case FastUtilCompact:
            return new FastUtilCompactWordIndex(editDistanceMax, verbosity);
        case KolobokeCompact:
            return new KolobokeCompactWordIndex(editDistanceMax, verbosity);
        case TroveCompact:
            return new TroveCompactWordIndex(editDistanceMax, verbosity);
        case HashMap:
            return new HashMapWordIndex(editDistanceMax, verbosity);
        case HashedWord:
            return new HashedWordIndex(editDistanceMax, verbosity);
        case FastIntUtilCompact:
            return new FastIntUtilCompactWordIndex(editDistanceMax, verbosity);
        }
        return null;
    }
}
