package org.comroid.test.model;

public interface AnotherPartialAbstract {
    double getAnother();

    default double sub(double from) {
        return from - getAnother();
    }
}
