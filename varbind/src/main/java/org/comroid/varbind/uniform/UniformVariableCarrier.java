package org.comroid.varbind.uniform;

import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;
import org.comroid.varbind.VariableCarrier;

import org.jetbrains.annotations.Nullable;

public class UniformVariableCarrier<BAS, OBJ extends BAS, ARR extends BAS>
        extends VariableCarrier<UniNode<BAS>, UniObjectNode<BAS, OBJ>, UniArrayNode<BAS, ARR>> {
}
