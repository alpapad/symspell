package com.faroo.symspell.string;

import java.lang.reflect.Field;

@SuppressWarnings("restriction")
enum ModernHotSpotStringGetter implements Getter{
    INSTANCE;

    private static final long valueOffset;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueOffset = UnsafeAccess.UNSAFE.objectFieldOffset(valueField);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public char[] get(String s) {
        return  (char[]) UnsafeAccess.UNSAFE.getObject(s, valueOffset);
    }
}