package com.faroo.symspell;

import java.util.List;

public interface ISymSpell {

	List<SuggestItem> lookup(String inputStr, int editDistanceMax);

	List<SuggestItem> lookup(String inputStr, Verbosity verbosity, int editDistanceMax);

	int getMaxLength();

	int getWordCount();

	int getEntryCount();

}