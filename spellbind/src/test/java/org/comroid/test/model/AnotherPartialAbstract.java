package org.comroid.test.model;

public interface AnotherPartialAbstract {
    default double sub(double from) {
        return from - getAnother();
    }

    double getAnother();
}
