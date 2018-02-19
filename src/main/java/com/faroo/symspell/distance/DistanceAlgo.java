package com.faroo.symspell.distance;

public enum DistanceAlgo {
	DamerauLevenshtein, //
	Levenshtein, //
	LimitedLevenshtein, //
	RestrictedDamerauLevenshtein;

	public IDistance instance() {
		switch (this) {
		case DamerauLevenshtein:
			return new DamerauLevenshteinDistance();
		case Levenshtein:
			return new LevenshteinDistance();
		case LimitedLevenshtein:
			return new LimitedLevenshteinDistance();
		case RestrictedDamerauLevenshtein:
			return new RestrictedDamerauLevenshteinDistance();
		default:
			throw new UnsupportedOperationException();
		}
	}
}
