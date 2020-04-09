package org.comroid.test.model;

import org.comroid.varbind.GroupBind;
import org.comroid.varbind.VarBind;
import org.comroid.varbind.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.adapter.data.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(Role.Binds.class)
public class Role extends VariableCarrier<DiscordAPI> {
    public Role(DiscordAPI dependencyObject, JSONObject data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @VarBind.Root GroupBind GROUP = new GroupBind(
                fastJsonLib, "role");
    }
}
