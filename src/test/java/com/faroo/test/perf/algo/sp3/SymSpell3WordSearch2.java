package com.faroo.test.perf.algo.sp3;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.distance.DistanceAlgo;
import com.faroo.symspell.impl.v3.IWordIndex;
import com.faroo.symspell.impl.v3.IndexAlgo;
import com.faroo.symspell.impl.v3.Indexer;
import com.faroo.symspell.impl.v3.SymSpellV3;
import com.faroo.test.perf.WordSearch2;


public class SymSpell3WordSearch2 implements WordSearch2 {

	boolean commited = false;
	private SymSpellV3 symSpell;
	private final int distance;

	public SymSpell3WordSearch2(int distance) {
		this.distance = distance;
		//this.symSpell = new SymSpellV3(this.distance, Verbosity.All ,DistanceAlgo.OptimalStringAlignment);
	}

	public void finishIndexing() {
		//this.symSpell.commit();
	}



	@Override
	public List<String> findSimilarWords(String searchQuery) {
		return symSpell.lookup(searchQuery.trim().toLowerCase(),  this.distance)//
				.stream()//
				.map(item -> item.term).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

    @Override
    public boolean loadIndex(String indexFile) {
        Indexer indx = new Indexer(IndexAlgo.FastUtilCompact, this.distance, Verbosity.All);
        IWordIndex index;
        try {
            index = indx.load(indexFile);
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        this.symSpell = new SymSpellV3(this.distance,  Verbosity.All, DistanceAlgo.OptimalStringAlignment, index);
        return true;
    }

}
