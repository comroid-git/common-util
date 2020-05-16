package org.comroid.commandline;

import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockParser {
    public final Map<String, Reference.Settable<String>> yields = new ConcurrentHashMap<>();//TrieMap.ofString();

    private String prevName = null;

    public void append(String arg) {
        final int len = arg.length();
        final String name = arg.contains("-") ? arg.substring(arg.lastIndexOf('-') + 1) : arg;

        if (arg.startsWith("-")) {
            if (!arg.startsWith("--")) {
                if (len > 2) {
                    if (arg.contains("=")) {
                        // handle special case where = is used to define value of argument
                        final String[] pair = name.split("=");

                        compute(pair[0]).set(pair[1]);
                        prevName = pair[0];
                        return;
                    } else {
                        // parse single chars
                        name.chars().forEach(ic -> {
                            compute(String.valueOf((char) ic));
                            prevName = null;
                        });

                        return;
                    }
                } else assert prevName == null;
            }

            compute(name);
            if (name.length() > 1)
                prevName = name;
        } else {
            if (prevName != null) {
                final Reference.Settable<String> compute = compute(prevName);
                compute.set(arg);
                prevName = null;
            }
        }
    }

    @NotNull
    private Reference.Settable<String> compute(String name) {
        return yields.computeIfAbsent(name, key -> Reference.Settable.create());
    }
}
