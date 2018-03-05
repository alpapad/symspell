package com.faroo.symspell.map;

/**
 * 
 * Represents an operation that accepts
 * 
 * a {@code long}-valued and
 * an object-valued argument,
 * 
 * and returns no result.  This is
 * the {@code (long,
 * reference)} specialization of {@link BiConsumer}.
 * Unlike most other functional interfaces, {@code LongObjConsumer} is
 * expected to operate via side-effects.
 *
 * 
 * @param <U> the type of the second argument the operation
 * @see BiConsumer
 * 
 */
@FunctionalInterface
public interface LongObjConsumer<U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param a the first input argument
     * @param b the second input argument
     */
    void accept(long a, U b);
}
