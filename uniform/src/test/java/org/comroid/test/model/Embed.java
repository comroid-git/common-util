package org.comroid.test.model;

import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainerBase;

import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.adapter.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(Embed.Binds.class)
public class Embed extends DataContainerBase<DiscordAPI> {
    public Embed(DiscordAPI dependencyObject, JSONObject data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @VarBind.Root GroupBind GROUP = new GroupBind(fastJsonLib, "embed", invocable);
    }
}
