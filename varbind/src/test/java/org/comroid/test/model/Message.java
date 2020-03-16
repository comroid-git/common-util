package org.comroid.test.model;

import java.util.ArrayList;
import java.util.Collection;

import org.comroid.varbind.ArrayBind;
import org.comroid.varbind.GroupBind;
import org.comroid.varbind.VarBind;
import org.comroid.varbind.model.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(Message.Binds.class)
public class Message extends VariableCarrier<JSON, JSONObject, DiscordAPI> {
    protected Message(DiscordAPI api, JSONObject node) {
        super(fastJsonLib, node, api);
    }

    interface Binds {
        @VarBind.Root
        GroupBind<JSON, JSONObject, JSONArray> MESSAGE_BIND = new GroupBind<>(fastJsonLib, "message");
        VarBind.Uno<JSONObject, Long> ID = MESSAGE_BIND.bind1Stage("id", Long.class);
        ArrayBind.Dep<JSONObject, JSONObject, Reaction, DiscordAPI, Collection<Reaction>> REACTIONS = MESSAGE_BIND.listDependent("reactions", JSONObject.class, Reaction.Binds.GROUP.autoRemapper(Reaction.class, DiscordAPI.class), ArrayList::new);
    }
}
