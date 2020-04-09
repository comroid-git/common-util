package org.comroid.test.model;

import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.comroid.varbind.GroupBind;
import org.comroid.varbind.VarBind;
import org.comroid.varbind.VariableCarrier;

@VarBind.Location(NGinXFSNode.Bind.class)
public final class NGinXFSNode extends VariableCarrier<Void> {
    protected NGinXFSNode(UniObjectNode initialData) {
        super(FastJSONLib.fastJsonLib, initialData, null);
    }

    public String getName() {
        return get(Bind.Name);
    }

    public String getType() {
        return get(Bind.Type);
    }

    public String getMtime() {
        return get(Bind.MTime);
    }

    interface Bind {
        @VarBind.Root
        GroupBind root = new GroupBind(FastJSONLib.fastJsonLib, "fsnode");
        VarBind.Uno<String> Name  = root.bind1stage("name", UniValueNode.ValueType.STRING);
        VarBind.Uno<String> Type  = root.bind1stage("type", UniValueNode.ValueType.STRING);
        VarBind.Uno<String> MTime = root.bind1stage("mtime", UniValueNode.ValueType.STRING);
    }
}
