package org.comroid.commandline;

import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;
import org.comroid.trie.TrieMap;

import java.util.Map;

public final class CommandLineArgs implements ReferenceMap<String, String, Reference<String>> {
    private final TrieMap<String, String> values;

    private CommandLineArgs(TrieMap<String, String> values) {
        this.values = values;
    }

    public synchronized static CommandLineArgs parse(String[] args) {
        final BlockParser parser = new BlockParser();

        for (int i = 0; i < args.length; i++) {
            parser.append(args[i]);
        }

        return new CommandLineArgs(parser.yields);
    }

    public boolean hasFlag(char c) {
        return hasKey(String.valueOf(c));
    }

    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public Reference<String> getReference(String key, boolean createIfAbsent) {
        return values.getReference(key, createIfAbsent);
    }

    @Override
    public ReferenceIndex<Map.Entry<String, String>> entryIndex() {
        return values.entryIndex();
    }
}
