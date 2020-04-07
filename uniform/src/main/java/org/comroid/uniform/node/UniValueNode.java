package org.comroid.uniform.node;

import org.comroid.common.ref.Reference;

public final class UniValueNode<T> extends UniNode {
    private final Adapter<T> valueAdapter;

    public UniValueNode(API API, Adapter<T> valueAdapter) {
        super(API, Type.VALUE);

        this.valueAdapter = valueAdapter;
    }

    public interface Adapter<T> extends Reference<T> {
    }
}
