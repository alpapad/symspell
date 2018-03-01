package com.faroo.symspell.string;

enum JvmStringGetter implements Getter {
    INSTANCE;

    @Override
    public char[] get(String s) {
        return s.toCharArray();
    }
}