package com.faroo.test.unit;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.faroo.symspell.Verbosity;
import com.faroo.symspell.impl.v3.CompactWordIndex;

public class TestLoad {
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
        gc();
        
       
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
       
        CompactWordIndex dict2 = new CompactWordIndex(2,  Verbosity.All);
        
        try(ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream("/tmp/object.data")))) {
            dict2.readExternal(is);
        }
        gc();
        gc();
        long mem3 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        
        System.err.println(mem3/1024/1024);
        
        dict2 = new CompactWordIndex(2,  Verbosity.All);
        
        try(ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream("/tmp/object.data")))) {
            dict2.readExternal(is);
        }
        gc();
        gc();
        mem3 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) - mem;
        
        System.err.println(mem3/1024/1024);
    }

}
