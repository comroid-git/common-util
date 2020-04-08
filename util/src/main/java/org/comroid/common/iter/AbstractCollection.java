package org.comroid.common.iter;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public interface AbstractCollection<T> extends Collection<T> {
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    default boolean contains(Object o) {
        for (T it : this) {
            if (o.equals(it))
                return true;
        }

        return false;
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> objects) {
        return objects.stream()
                .allMatch(this::contains);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> objects) {
        boolean added = false;

        for (T object : objects) {
            if (add(object) && !added) added = true;
        }

        return added;
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> objects) {
        boolean removed = false;

        for (Object object : objects) {
            if (remove(object) && !removed)
                removed = true;
        }

        return removed;
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> keep) {
        boolean removed = false;

        if (keep.size() > size()) {
            for (Object k : keep) {
                for (T each : this) {
                    if (!k.equals(each) && remove(each)) removed = true;
                }
            }
        } else {
            for (T each : this) {
                for (Object k : keep) {
                    if (!k.equals(each) && remove(each)) removed = true;
                }
            }
        }

        return removed;
    }
}
