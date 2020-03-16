package org.comroid.test.model;

import org.comroid.varbind.GroupBind;
import org.comroid.varbind.model.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

public class Reaction extends VariableCarrier<JSON, JSONObject, DiscordAPI> {
    protected Reaction(DiscordAPI dependencyObject, String data) {
        super(fastJsonLib, data, dependencyObject);
    }

    public interface Binds {
        GroupBind<JSON, JSONObject, JSONArray> GROUP = new GroupBind<>(fastJsonLib, "reaction");
    }
}
