package org.comroid.uniform.node;

import java.util.List;

public final class UniArrayNode extends UniNode {
    private final Adapter arrayAdapter;

    public UniArrayNode(API API, Adapter arrayAdapter) {
        super(API, Type.ARRAY);

        this.arrayAdapter = arrayAdapter;
    }

    public interface Adapter extends List<Object> {
    }
}
