package org.comroid.test.model;

public interface PartialAbstract {
    int getValue();

    default int add(int to) {
        return getValue() + to;
    }
}
