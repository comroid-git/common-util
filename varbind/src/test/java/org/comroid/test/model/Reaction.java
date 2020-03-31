package org.comroid.test.model;

import org.comroid.varbind.GroupBind;
import org.comroid.varbind.VarBind;
import org.comroid.varbind.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(Reaction.Binds.class)
public class Reaction extends VariableCarrier<JSON, JSONObject, DiscordAPI> {
    public Reaction(DiscordAPI dependencyObject, JSONObject data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        @VarBind.Root GroupBind<JSON, JSONObject, JSONArray> GROUP = new GroupBind<>(
                fastJsonLib, "reaction");
    }
}
