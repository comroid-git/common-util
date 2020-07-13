package org.comroid.mutatio.pipe;

import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class SortedResultingPipe<T> extends BasicPipe<T, T> implements Pipe<T, T> {
    private final Comparator<? super T> comparator;

    @Override
    public boolean isSorted() {
        return true;
    }

    public SortedResultingPipe(Pipe<?, T> base, Comparator<? super T> comparator) {
        super(base);
        this.comparator = comparator;
    }

    @Override
    public Reference<T> getReference(int index) {
        class SubRef extends Reference.Support.Base<T> {
            private final int accessedIndex;

            @Override
            public boolean isOutdated() {
                return true;
            }

            public SubRef(int accessedIndex) {
                super(null, false);

                this.accessedIndex = accessedIndex;
            }

            @Nullable
            @Override
            public T doGet() {
                final List<T> sorted = refs.unwrap();
                sorted.sort(comparator);
                if (accessedIndex < sorted.size()) {
                    T v;
                    if ((v = sorted.get(accessedIndex)) != null)
                        return v;
                }
                return null;
            }
        }

        return new SubRef(index);
    }
}
