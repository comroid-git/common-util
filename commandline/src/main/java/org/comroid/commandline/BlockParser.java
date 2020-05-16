package org.comroid.commandline;

import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockParser {
    public final Map<String, Reference.Settable<String>> yields = TrieMap.ofString();

    private String prevName = null;

    public void append(String arg) {
        final int len = arg.length();
        final String name = arg.substring(arg.lastIndexOf('-'));

        if (arg.startsWith("-")) {
            assert prevName == null;
            if (!arg.startsWith("--")) {
                if (len > 2) {
                    if (arg.contains("=")) {
                        // handle special case where = is used to define value of argument
                        final String[] pair = name.split("=");

                        prevName = pair[0];
                        compute().set(pair[1]);
                    } else {
                        // parse single chars
                        name.chars().forEach(ic -> {
                            prevName = String.valueOf(ic);
                            compute();
                        });
                    }
                }
                return;
            }

            prevName = name;
            compute();
        } else {
            if (prevName != null) {
                compute().set(arg);
                prevName = null;
            }
        }
    }

    @NotNull
    private Reference.Settable<String> compute() {
        return yields.computeIfAbsent(prevName, key -> Reference.Settable.create());
    }
}
