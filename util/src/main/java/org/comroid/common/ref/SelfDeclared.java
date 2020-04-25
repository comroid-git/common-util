package org.comroid.common.ref;

/**
 * fuck naming
 */
public interface SelfDeclared<S extends SelfDeclared<? super S>> {
    default S self() {
        //noinspection unchecked
        return (S) this;
    }
}
