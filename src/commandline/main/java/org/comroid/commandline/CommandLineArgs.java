package org.comroid.commandline;

import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.Reference;
import org.comroid.mutatio.ref.ReferenceIndex;
import org.comroid.mutatio.ref.ReferenceMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandLineArgs extends ReferenceMap.Support.Basic<String, String> {
    private CommandLineArgs(Map<String, KeyedReference<String, String>> values) {
        super(values);
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
        return containsKey(key);
    }

    public boolean hasName(String key) {
        return !key.isEmpty() && (hasKey(key) || hasFlag(key.charAt(0)));
    }

    public boolean hasValueAt(String key) {
        return getReference(key).isNonNull();
    }
}
