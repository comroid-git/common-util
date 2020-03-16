package org.comroid.test.model;

import java.util.Collection;

import org.comroid.varbind.bind.ArrayBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.model.VariableCarrier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

@VarBind.Location(fieldName = "MESSAGE_BIND", inClass = Message.Binds.class)
public class Message extends VariableCarrier<JSON, JSONObject, DiscordAPI> {
    protected Message(DiscordAPI api, JSONObject node) {
        super(fastJsonLib, node, api);
    }

    public static final class Binds {
        public static final GroupBind<JSON, JSONObject, JSONArray> MESSAGE_BIND = new GroupBind<>(fastJsonLib, "message");
        public static final VarBind.Uno<JSONObject, Long> ID = MESSAGE_BIND.bind1Stage("id", JSONObject::getLongValue);
        public static final ArrayBind.Duo<JSONObject, JSONObject, Reaction, Collection<Reaction>> REACTIONS
                = MESSAGE_BIND.list2Stage("reactions", JSONObject.class, )
    }
}
