package org.comroid.test.model;

import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.container.DataContainerBase;

import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.adapter.json.fastjson.FastJSONLib.fastJsonLib;

@Location(Role.Binds.class)
public class Role extends DataContainerBase<DiscordAPI> {
    public Role(DiscordAPI dependencyObject, JSONObject data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @RootBind
        GroupBind GROUP = new GroupBind(fastJsonLib, "role", invocable);
    }
}
