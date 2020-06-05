package org.comroid.matrix.impl;

import org.comroid.matrix.Matrix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PartialMatrix<V, E extends Matrix.Entry<V>> extends AbstractMatrix<V, E> {
    public PartialMatrix(Map<String, E> underlying) {
        super(underlying);
    }

    @NotNull
    @Override
    protected E createEntry(String key, @Nullable V initialValue) {
        throw new UnsupportedOperationException("Cannot create entries");
    }
}
