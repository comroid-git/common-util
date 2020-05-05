package org.comroid.test.model;

import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.Root;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainerBase;

@Location(NGinXFSNode.Bind.class)
public final class NGinXFSNode extends DataContainerBase<Void> {
    protected NGinXFSNode(UniObjectNode initialData) {
        super(, FastJSONLib.fastJsonLib, initialData, null);
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
        @Root
        GroupBind root = new GroupBind(FastJSONLib.fastJsonLib, "fsnode", invocable);
        VarBind.OneStage<String> Name  = root.bind1stage("name", UniValueNode.ValueType.STRING);
        VarBind.OneStage<String> Type  = root.bind1stage("type", UniValueNode.ValueType.STRING);
        VarBind.OneStage<String> MTime = root.bind1stage("mtime", UniValueNode.ValueType.STRING);
    }
}
