package org.comroid.common.ref;

/**
 * fuck naming
 */
public interface SelfDeclared<S extends SelfDeclared<S>> {
    S self();
}
