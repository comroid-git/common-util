package org.comroid.restless.socket;

import org.comroid.uniform.node.UniNode;

@FunctionalInterface
public interface SocketListener {
    void onDataReceived(UniNode data);
}
