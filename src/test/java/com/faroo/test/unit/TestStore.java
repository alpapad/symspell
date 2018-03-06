package com.faroo.test.unit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v3.FastUtilCompactWordIndex;
public class TestStore {
    static void gc() {
        System.err.println("Gc...");
        for (int i = 0; i < 4; i++) {
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
        
        FastUtilCompactWordIndex dict = new FastUtilCompactWordIndex(2,  Verbosity.All);
        
        System.err.println("Index");
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        int i=0;
        for (String w : data.loadCorpus()) {
            i++;
            dict.addWord(w.toLowerCase());
            if(i%1000==0) {
                System.err.print(".");
            }
            if(i%80000==0) {
                System.err.println(".");
            }
        }
        dict.commit();
        System.err.println("Done...");
        gc();
        gc();
        long mem2 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        System.err.println("Mem:" + mem2/1024/1024);
        
        System.err.println("Save");
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream("/tmp/object.data"),16*1024)))) {
            dict.writeExternal(objectOutputStream);
        }
        System.err.println("Done saving");
        dict = null;
        
        gc();
        gc();
        System.err.println("Load");
        mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        FastUtilCompactWordIndex dict2 = new FastUtilCompactWordIndex(2,  Verbosity.All);
        
        try(ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream("/tmp/object.data"))))) {
            dict2.readExternal(is);
        }
        System.err.println("Done Loading");
        gc();
        gc();
        long mem3 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        
        System.err.println("Mem:" + mem2/1024/1024 + " --- " + mem3/1024/1024);
        mem = mem3;
        
        dict2 = new FastUtilCompactWordIndex(2,  Verbosity.All);
        gc();
        gc();
        System.err.println("Load");
        try(ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream("/tmp/object.data"))))) {
            dict2.readExternal(is);
        }
        System.err.println("Done Loading");
        gc();
        gc();
        mem3 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        
        System.err.println("Mem:" + mem2/1024/1024 + " --- " + mem3/1024/1024);
    }

}
