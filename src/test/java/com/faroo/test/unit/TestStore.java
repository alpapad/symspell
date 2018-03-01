package com.faroo.test.unit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.distance.DistanceAlgo;
import com.faroo.symspell.impl.v3.HashKeySimpleDictionary;
import com.faroo.symspell.impl.v3.SymSpellV3;

public class TestStore {
    static void gc() {
        for (int i = 0; i < 2; i++) {
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        TestData data = new TestData();
        gc();
        gc();
        
        HashKeySimpleDictionary dict = new HashKeySimpleDictionary(2,  Verbosity.All);
        
        System.err.println("Index");
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        SymSpellV3 sp = new SymSpellV3(3, Verbosity.All, DistanceAlgo.OptimalStringAlignment, dict);
        for (String w : data.loadCorpus()) {
            sp.createDictionaryEntry(w.toLowerCase());
        }
        sp.commit();
        gc();
        gc();
        System.err.println("Save");
        long mem2 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("/tmp/object.data"))) {
            dict.writeExternal(objectOutputStream);
        }
        dict = null;
        sp = null;
        System.err.println("Load");
        gc();
        gc();
        mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        HashKeySimpleDictionary dict2 = new HashKeySimpleDictionary(2,  Verbosity.All);
        
        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream("/tmp/object.data"))) {
            dict2.readExternal(is);
        }
        long mem3 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        
        System.err.println(mem2/1024/1024 + " --- " + mem3/1024/1024);
    }

}
