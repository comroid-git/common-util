package org.comroid.api;

import java.util.UUID;

public class UUIDContainer {
    private final UUID id = UUID.randomUUID();

    public UUID getUUID() {
        return id;
    }

    public static abstract class Seeded extends UUIDContainer {
        private final UUID id = UUID.nameUUIDFromBytes(idSeed().getBytes());

        @Override
        public UUID getUUID() {
            return id;
        }

        protected abstract String idSeed();
    }
}
