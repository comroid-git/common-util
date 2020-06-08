package org.comroid.commandline;

import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceMap;

import java.util.Collections;
import java.util.Map;

public final class CommandLineArgs implements ReferenceMap<String, String, Reference<String>> {
    private final Map<String, Reference<String>> values;

    private CommandLineArgs(Map<String, Reference<String>> values) {
        this.values = values;
    }

    public synchronized static CommandLineArgs parse(String[] args) {
        final BlockParser parser = new BlockParser();

        for (int i = 0; i < args.length; i++) {
            parser.append(args[i]);
        }

        return new CommandLineArgs(Collections.unmodifiableMap(parser.yields));
    }

    public boolean hasFlag(char c) {
        return hasKey(String.valueOf(c));
    }

    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public Reference<String> getReference(String key, boolean createIfAbsent) {
        return createIfAbsent
                ? values.computeIfAbsent(key, k -> Reference.empty())
                : values.getOrDefault(key, Reference.empty());
    }
}
