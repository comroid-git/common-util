package org.comroid.test.model;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.comroid.common.Polyfill;
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
    public Message(DiscordAPI api, JSONObject node) {
        super(fastJsonLib, node, api);
    }

    // get for non-optional field
    public final long getID() {
        return getVar(Binds.ID);
    }

    // getter for optional field
    public final Optional<String> getEditedTimestamp() {
        // if wrapVar is not used, getVar might return null
        return wrapVar(Binds.EDITED_TIMESTAMP);
    }

    public interface Binds {
        @VarBind.Root
        GroupBind<JSON, JSONObject, JSONArray> GROUP
                = new GroupBind<>(fastJsonLib, "message");
        ArrayBind.Duo<JSONObject, String, URL, Collection<URL>> ATTACHMENTS
                = GROUP.list2Stage("attachments", String.class, spec -> Polyfill.url(spec), ArrayList::new);
        ArrayBind.Dep<JSONObject, JSONObject, Reaction, DiscordAPI, Collection<Reaction>> REACTIONS
                = GROUP.listDependent("reactions", JSONObject.class, DiscordAPI::parseReaction, ArrayList::new);
        VarBind.Uno<JSONObject, Boolean> TTS
                = GROUP.bind1Stage("tts", Boolean.class);
        ArrayBind.Dep<JSONObject, JSONObject, Embed, DiscordAPI, Collection<Embed>> EMBEDS
                = GROUP.listDependent("embeds", JSONObject.class, Embed.Binds.GROUP.autoRemapper(Embed.class, DiscordAPI.class), ArrayList::new);
        VarBind.Duo<JSONObject, String, String> TIMESTAMP
                = GROUP.bind2Stage("timestamp", String.class, spec -> spec); // todo Instant parsing
        VarBind.Uno<JSONObject, Boolean> MENTIONS_EVERYONE
                = GROUP.bind1Stage("mention_everyone", Boolean.class);
        VarBind.Uno<JSONObject, Long> ID
                = GROUP.bind1Stage("id", Long.class);
        VarBind.Uno<JSONObject, Boolean> PINNED
                = GROUP.bind1Stage("pinned", Boolean.class);
        VarBind.Duo<JSONObject, String, String> EDITED_TIMESTAMP
                = GROUP.bind2Stage("edited_timestamp", String.class, spec -> spec); // todo Instant parsing
        VarBind.Dep<JSONObject, JSONObject, User, DiscordAPI> AUTHOR
                = GROUP.bindDependent("author", JSONObject.class, User.Binds.GROUP.autoRemapper(User.class, DiscordAPI.class));
        ArrayBind.Dep<JSONObject, JSONObject, Role, DiscordAPI, Collection<Role>> MENTIONED_ROLES
                = GROUP.listDependent("mentioned_roles", JSONObject.class, Role.Binds.GROUP.autoRemapper(Role.class, DiscordAPI.class), ArrayList::new);
        VarBind.Uno<JSONObject, String> CONTENT
                = GROUP.bind1Stage("content", String.class);
        VarBind.Dep<JSONObject, Long, Channel, DiscordAPI> CHANNEL
                = GROUP.bindDependent("channel_id", Long.class, DiscordAPI::getChannelById);
        ArrayBind.Dep<JSONObject, JSONObject, User, DiscordAPI, Collection<User>> MENTIONED_USERS
                = GROUP.listDependent("mentions", JSONObject.class, User.Binds.GROUP.autoRemapper(User.class, DiscordAPI.class), ArrayList::new);
        VarBind.Duo<JSONObject, Integer, Message.Type> TYPE
                = GROUP.bind2Stage("type", Integer.class, Type::new);
    }

    public static class Type {
        public final int value;

        private Type(int value) {
            this.value = value;
        }
    }
}
