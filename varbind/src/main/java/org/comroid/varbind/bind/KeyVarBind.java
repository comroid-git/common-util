package org.comroid.varbind.bind;

import java.util.Map;

public interface KeyVarBind<K, T> extends Map<K, VarBind<T>>, GroupedBind {
}
