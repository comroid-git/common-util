package org.comroid.common.ref;

/**
 * fuck naming
 */
public interface SelfDeclared<S extends SelfDeclared<S>> {
    default S self() {
        return (S) this;
    }
}
