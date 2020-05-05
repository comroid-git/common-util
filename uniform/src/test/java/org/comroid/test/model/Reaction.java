package org.comroid.test.model;

import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.Root;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.container.DataContainerBase;

import static org.comroid.uniform.adapter.json.fastjson.FastJSONLib.fastJsonLib;

@Location(Reaction.Binds.class)
public class Reaction extends DataContainerBase<DiscordAPI> {
    public Reaction(DiscordAPI dependencyObject, UniObjectNode data) {
        super(, fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @Root
        GroupBind GROUP = new GroupBind(fastJsonLib, "reaction", invocable);
    }
}
