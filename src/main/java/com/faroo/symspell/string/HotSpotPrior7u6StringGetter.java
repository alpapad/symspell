package com.faroo.symspell.string;

import java.lang.reflect.Field;
import java.util.Arrays;

@SuppressWarnings("restriction")
public enum HotSpotPrior7u6StringGetter implements Getter{
    INSTANCE;

    private static final long valueOffset;
    private static final long offsetOffset;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueOffset = UnsafeAccess.UNSAFE.objectFieldOffset(valueField);

            Field offsetField = String.class.getDeclaredField("offset");
            offsetOffset = UnsafeAccess.UNSAFE.objectFieldOffset(offsetField);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public char[] get(String s) {
        char[] value =  (char[]) UnsafeAccess.UNSAFE.getObject(s, valueOffset);
        int offset = UnsafeAccess.UNSAFE.getInt(s, offsetOffset);
        return Arrays.copyOfRange(value, offset, s.length());
    }
}
