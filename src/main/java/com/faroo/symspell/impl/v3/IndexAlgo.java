package com.faroo.symspell.impl.v3;

import com.faroo.symspell.Verbosity;

public enum IndexAlgo {
    CustomCompact,
    FastUtilCompact,
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
        }
        return null;
    }
}
