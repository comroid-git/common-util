package org.comroid.test.model;

import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.Root;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.container.DataContainerBase;

import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.adapter.json.fastjson.FastJSONLib.fastJsonLib;

@Location(User.Binds.class)
public class User extends DataContainerBase<DiscordAPI> {
    public User(DiscordAPI dependencyObject, JSONObject data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @Root
        GroupBind GROUP = new GroupBind(fastJsonLib, "user", invocable);
    }
}
