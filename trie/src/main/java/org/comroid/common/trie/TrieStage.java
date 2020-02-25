package org.comroid.common.trie;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

public class TrieStage<V> {
    private final Map<Character, TrieStage<V>> subStages = new ConcurrentHashMap<>();
    @Nullable V value;

    @Nullable V get(char[] chars, int index) {
        if (chars.length == 0 || index >= chars.length)
            return value;

        return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                .get(chars, index + 1);
    }

    @Nullable V set(char[] chars, int index, @Nullable V value) {
        if (chars.length == 0 || index >= chars.length) {
            final V old = this.value;
            this.value = value;

            return old;
        }

        return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                .set(chars, index + 1, value);
    }

    @Nullable V remove(char[] chars, int index) {
        if (chars.length == 0 || index + 1 >= chars.length)
            return subStages.remove(chars[index + 1]).value;

        return subStages.computeIfAbsent(chars[index], it -> new TrieStage<>())
                .remove(chars, index + 1);
    }

    int size() {
        return subStages.values()
                .stream()
                .mapToInt(TrieStage::size)
                .sum() + (Objects.isNull(value) ? 0 : 1);
    }

    Stream<TrieStage<V>> stream() {
        return Stream.concat(Stream.of(this), subStages.values()
                .stream()
                .flatMap(TrieStage::stream));
    }
}
