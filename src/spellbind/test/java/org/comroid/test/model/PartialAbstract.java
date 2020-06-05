package org.comroid.test.model;

public interface PartialAbstract {
    default int add(int to) {
        return getValue() + to;
    }

    int getValue();
}
