package org.comroid.common;

import java.util.regex.Matcher;

import org.jetbrains.annotations.Nullable;

public final class Polyfill {
    public static String regexGroupOrDefault(Matcher matcher, String groupName, @Nullable String orDefault) {
        String cont;

        if (matcher.matches() && (cont = matcher.group(groupName)) != null) return cont;
        else if (orDefault != null) return orDefault;
        else throw new NullPointerException("Group cannot be matched!");
    }
}
