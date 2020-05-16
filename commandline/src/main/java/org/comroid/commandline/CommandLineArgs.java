package org.comroid.commandline;

import org.comroid.common.map.ReferenceMap;
import org.comroid.common.ref.Reference;

import java.util.Collections;
import java.util.Map;

public final class CommandLineArgs implements ReferenceMap<String, String> {
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
    public Reference<String> getReference(String key) {
        return values.getOrDefault(key, Reference.empty());
    }
}
