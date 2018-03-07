package com.faroo.test.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v3.IndexAlgo;
import com.faroo.symspell.impl.v3.Indexer;
import com.ibm.icu.text.Transliterator;

public class IndexerTest {
    
    static String id = "Any-Latin; nfd; [:nonspacing mark:] remove; nfc";
    static Transliterator ts = Transliterator.getInstance(id);
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        Indexer indx = new Indexer(IndexAlgo.FastUtilCompact, 2, Verbosity.All);
        loadDictionary(indx, "src/test/resources/test_data/test_corpus.csv");
        indx.save("src/test/resources/test_data/test_corpus.idx");
    }


    static void loadDictionary(Indexer indx, String corpus) throws FileNotFoundException, IOException {
        File f = new File(corpus);
        if (!(f.exists() && !f.isDirectory())) {
            System.out.println("File not found: " + corpus);
            return;
        }
        System.err.println("Indexing file:"  + corpus);
        try (BufferedReader br = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] l1 = line.split("\\s+");
                if (l1.length > 0) {
                    indx.addWord(ts.transform(l1[0].trim().toLowerCase()));
                }
            }
        }
        System.err.println("Commit");
        indx.commit();
        System.err.println("Done");
    }
}
