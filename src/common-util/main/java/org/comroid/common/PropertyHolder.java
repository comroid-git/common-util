package org.comroid.common;

import org.comroid.api.UUIDContainer;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface PropertyHolder extends UUIDContainer {
    default ReferenceMap<String, Object, KeyedReference<String, Object>> getPropertyCache() {
        return Support.getCache(this);
    }

    @Internal
    final class Support {
        private static final Map<UUID, Support.ObjectCache> cache = new ConcurrentHashMap<>();

        private static ObjectCache getCache(PropertyHolder holder) {
            return cache.computeIfAbsent(holder.getUUID(), k -> new ObjectCache(holder));
        }

        private static final class ObjectCache implements ReferenceMap<String, Object, KeyedReference<String, Object>> {
            private final ReferenceIndex<KeyedReference<String, Object>> entryIndex = ReferenceIndex.create();
            private final PropertyHolder owner;

            private ObjectCache(PropertyHolder owner) {
                this.owner = owner;
            }

            @Nullable
            @Override
            public KeyedReference<String, Object> getReference(String key, boolean createIfAbsent) {
                return entryIndex.stream()
                        .filter(entry -> entry.getKey().equals(key))
                        .findFirst()
                        .orElseGet(() -> {
                            if (createIfAbsent) {
                                final KeyedReference<String, Object> ref = KeyedReference.create(key);
                                entryIndex.add(ref);
                                return ref;
                            } else return null;
                        });
            }

            @Override
            public ReferenceIndex<? extends Map.Entry<String, Object>> entryIndex() {
                return entryIndex;
            }
        }
    }
}
