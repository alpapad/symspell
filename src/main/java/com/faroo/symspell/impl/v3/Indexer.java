package com.faroo.symspell.impl.v3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.faroo.symspell.Verbosity;

public class Indexer {

    private final IWordIndex index;
    
    public Indexer(IndexAlgo algo, int editDistanceMax, Verbosity verbosity) {
        super();
        this.index = algo.instance(editDistanceMax, verbosity);
    }
    
    public boolean addWord(String word) {
        return index.addWord(word);
    }

    public int getMaxLength() {
        return index.getMaxLength();
    }

    public void commit() {
        index.commit();
    }

    public int getWordCount() {
        return index.getWordCount();
    }

    public int getDistance() {
        return index.getDistance();
    }

    public Verbosity getVerbosity() {
        return index.getVerbosity();
    }

    public IWordIndex load(String file) throws FileNotFoundException, ClassNotFoundException, IOException {
        return load(index, file);
    }

    public long save(String file) throws FileNotFoundException, IOException {
        return save(index, file); 
    }

    public static IWordIndex load(IndexAlgo algo, String file) throws FileNotFoundException, ClassNotFoundException, IOException {
        return load(algo.instance(2, Verbosity.All), file);
    }

    public static IWordIndex load(IWordIndex index, String file) throws FileNotFoundException, IOException, ClassNotFoundException {
        try(ObjectInputStream is = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
            index.readExternal(is);
        }
        return index;
    }

    public static long save(IWordIndex index, String file) throws FileNotFoundException, IOException {
        File in = new File(file);
        try (ObjectOutputStream objectOutputStream = //
                new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(in),16*1024)))) {
            index.writeExternal(objectOutputStream);
        }
        return Files.size(in.toPath());
    }
}
