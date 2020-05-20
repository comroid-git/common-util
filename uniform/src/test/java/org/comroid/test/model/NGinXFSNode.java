package org.comroid.test.model;

import org.comroid.uniform.ValueType;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
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
        @RootBind
        GroupBind root = new GroupBind(FastJSONLib.fastJsonLib, "fsnode", invocable);
        VarBind.OneStage<String> Name  = root.bind1stage("name", ValueType.STRING);
        VarBind.OneStage<String> Type  = root.bind1stage("type", ValueType.STRING);
        VarBind.OneStage<String> MTime = root.bind1stage("mtime", ValueType.STRING);
    }
}
