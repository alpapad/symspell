package com.faroo.symspell.string;



import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
final class UnsafeAccess {
    public static final UnsafeAccess INSTANCE = new UnsafeAccess();

    static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private UnsafeAccess() {}
}