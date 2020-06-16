package org.comroid.api;

import java.util.UUID;

public class UUIDContainer {
    private final UUID id = UUID.randomUUID();

    public UUID getUUID() {
        return id;
    }
}
