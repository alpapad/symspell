package com.faroo.test.primes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.primes.Primes;
import org.apache.commons.math3.util.Combinations;

public class Test {

    static Map<Character, Integer> charsMap = new HashMap<>();

    static Set<Long> primeKTable = new HashSet<>();

    public static void main(String[] args) {
        
        
        // TODO Auto-generated method stub
        List<Integer> primes = Generate.sieveOfEratosthenes(1000);

        System.err.println(primes);
        int i = 0;
        // Map<Character,Integer> charsMap = new HashMap<>();
        for (char s = 'a'; s <= 'z'; s++) {
            charsMap.put(s, primes.get(i++));
        }
        System.err.println(charsMap);

        System.err.println(strToPrim("abba"));
        System.err.println(strToPrim("aabb"));
        System.err.println(strToPrim("aba"));

        // System.err.println(strToPrim("abba").divide(strToPrim("aba")));
        // System.err.println(strToPrim("abba").divide(strToPrim("aa")));

        String[] database = { "a", "b", "c", "d" };
        for (i = 1; i <= database.length; i++) {
            String[] result = getAllLists(database, i);
            for (int j = 0; j < result.length; j++) {
                System.out.println(result[j]);
            }
        }
        dumpMax();

        primeMap(1);
        doTest();
        primeMap(2);
        doTest();
        primeMap(3);
        doTest();
//        primeMap(5);
//        doTest();
        
    }

    static void doTest() {
        System.err.println("1:" + isIn("abba", "aba"));
        System.err.println("2:" + isIn("abba", "aa"));
        System.err.println("3:" + isIn("abba", "aadds"));
        System.err.println("4:" + isIn("aabb", "aadds"));
        System.err.println("5:" + isIn("dds", "aadds"));
        System.err.println("6:" + isIn("ddd", "bbb"));
    }
    
    static boolean isIn(String a, String b) {
        BigInteger ai = strToPrim(a);
        BigInteger bi = strToPrim(b);
        System.err.println("A:" + a  + "=" + ai + ", B:" +b + "=" + bi + " A/B=" + ai.doubleValue()/bi.doubleValue() + ", B/A=" + bi.doubleValue()/ai.doubleValue());
        if(ai.compareTo(bi) > 0) {
            return primeKTable.contains(ai.divide(bi).longValueExact());
        } else {
            return primeKTable.contains(bi.divide(ai).longValueExact());
        }
    }
    static void dumpMax() {
        StringBuilder sb = new StringBuilder();
        for (int k = 1; k < 10; k++) {
            sb.append('z');
            System.err.println(String.format("%2d", k) + "  " + String.format("%30d", strToPrim(sb.toString())) + " --->" + sb.toString());
        }
    }

    static BigInteger strToPrim(String in) {
        BigInteger res = BigInteger.valueOf(1l);
        for (char c : in.toCharArray()) {
            res = res.multiply(BigInteger.valueOf(charsMap.get(c).longValue()));
        }
        return res;
    }

    static void primeMap(int dist) {
        List<Integer> primes = Generate.sieveOfEratosthenes(1000);
        Set<Long> products = new HashSet<>();
        primeKTable.clear();
        for(int k =1; k<= dist; k++) {
            Combinations a = new Combinations(30, k);
            Iterator<int[]> it = a.iterator();
            while (it.hasNext()) {
                int[] comb = it.next();
                BigInteger res = BigInteger.valueOf(1l);
                for(int i: comb) {
                    res = res.multiply(BigInteger.valueOf(primes.get(i)));
                }
                //System.err.println("-->" + res.longValue());
                products.add(res.longValue());
            }
        }
        //primeKTable.addAll(products);

        BigInteger one = BigInteger.valueOf(1l);
        primeKTable.add(one.longValue());
        for(Long pi: products) {
            // primeKTable.add(one.divide(pi));
            primeKTable.add(pi);
            for(Long pj: products) {
                if(pi>pj) {
                    primeKTable.add(pi/pj);
                }
            }
        }
        System.err.println("NUMBER OF COMBS FOR k = " + dist + " IS:" + primeKTable.size());// + "-->" + primeKTable);
    }

    public static String[] getAllLists(String[] elements, int lengthOfList) {
        // initialize our returned list with the number of elements calculated above
        String[] allLists = new String[(int) Math.pow(elements.length, lengthOfList)];

        // lists of length 1 are just the original elements
        if (lengthOfList == 1)
            return elements;
        else {
            // the recursion--get all lists of length 3, length 2, all the way up to 1
            String[] allSublists = getAllLists(elements, lengthOfList - 1);

            // append the sublists to each element
            int arrayIndex = 0;

            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < allSublists.length; j++) {
                    // add the newly appended combination to the list
                    allLists[arrayIndex] = elements[i] + allSublists[j];
                    arrayIndex++;
                }
            }
            return allLists;
        }
    }
}
