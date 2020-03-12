package org.comroid.varbind.model;

import java.util.Set;

import org.comroid.varbind.bind.VarBind;

public interface VariableCarrier {
    Set<VarBind<?>> getBindings();
}
