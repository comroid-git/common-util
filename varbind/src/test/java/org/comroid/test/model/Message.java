package org.comroid.test.model;

import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.model.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(Message.Binds.class)
public class Message extends VariableCarrier<JSON, JSONObject, DiscordAPI> {
    protected Message(DiscordAPI api, JSONObject node) {
        super(fastJsonLib, node, api);
    }

    public static final class Binds {
        public static final VarBind.Uno<JSONObject, Long> ID = new VarBind.Uno<>();
    }
}
