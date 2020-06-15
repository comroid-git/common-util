package org.comroid.test.varbind;

import org.comroid.varbind.container.DataContainerBase;

public final class Dummy extends DataContainerBase<Dummy> {
    public Dummy() {
        super(null);
    }

    public String modify(String str) {
        int mid = str.length() / 5;
        return str.substring(mid) + str.substring(0, mid);
    }
}
