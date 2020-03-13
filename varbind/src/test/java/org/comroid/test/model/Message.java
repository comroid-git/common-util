package org.comroid.test.model;

import java.time.Instant;

import org.comroid.varbind.VarBindFactory;
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

    public Channel getTimestamp() {
        return getVar(Binds.CHANNEL);
    }

    public static final class Binds {
        public static final VarBind.Uno<Long> ID = VarBindFactory.simple(fastJsonLib, "id", JSONObject::getLong);
        public static final VarBind.Uno<Boolean> TTS = VarBindFactory.simple(fastJsonLib, "tts", JSONObject::getBooleanValue);
        public static final VarBind.Duo<Instant, String> TIMESTAMP = VarBindFactory.mapped(fastJsonLib, "timestamp", JSONObject::getString, Instant::parse);
        public static final VarBind.Dep<Channel, Long, DiscordAPI> CHANNEL = VarBindFactory.dependent(fastJsonLib, "channel_id", JSONObject::getLong, DiscordAPI::getChannelById);
    }
}
