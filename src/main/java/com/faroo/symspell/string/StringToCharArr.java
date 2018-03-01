package com.faroo.symspell.string;

public class StringToCharArr {
    private static Getter getter;
    static  {
        try {
            if (System.getProperty("java.vm.name").contains("HotSpot")) {
                String javaVersion = System.getProperty("java.version");
                if (javaVersion.compareTo("1.7.0_06") >= 0) {
                    if (javaVersion.compareTo("1.9") >= 0) {
                        getter = JvmStringGetter.INSTANCE;
                    } else {
                        getter = ModernHotSpotStringGetter.INSTANCE;
                    }
                } else {
                    getter = HotSpotPrior7u6StringGetter.INSTANCE;
                }
            } else {
                // try to initialize this version anyway
                getter = HotSpotPrior7u6StringGetter.INSTANCE;
            }
        } catch (Throwable e) {
            // ignore
        } finally {
            if (getter == null)
                getter = JvmStringGetter.INSTANCE;
        }
    }
    
    public static char[] arr(String s) {
        return getter.get(s);
    }
}
