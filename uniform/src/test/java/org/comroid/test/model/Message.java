package org.comroid.test.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import org.comroid.common.Polyfill;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.Root;
import org.comroid.varbind.bind.ArrayBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainerBase;

import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.adapter.json.fastjson.FastJSONLib.fastJsonLib;

@Location(Message.Binds.class)
public final class Message extends DataContainerBase<DiscordAPI> {
    public Message(DiscordAPI api, JSONObject node) {
        super(fastJsonLib, node, api);
    }

    // get for non-optional field
    public final long getID() {
        return get(Binds.ID);
    }

    // getter for optional field
    public final Optional<String> getEditedTimestamp() {
        // if wrapVar is not used, getVar might return null
        return wrap(Binds.EDITED_TIMESTAMP);
    }

    public static class Type {
        public final int value;

        private Type(int value) {
            this.value = value;
        }
    }

    public interface Binds {
        @Root
        GroupBind GROUP = new GroupBind(fastJsonLib, "message", invocable);
        ArrayBind.TwoStage<String, URL, Collection<URL>> ATTACHMENTS       = GROUP.list2stage(
                "attachments",
                UniValueNode.ValueType.STRING,
                spec -> Polyfill.url(spec),
                ArrayList::new
        );
        ArrayBind.DependentTwoStage<UniObjectNode, DiscordAPI, Embed, Collection<Embed>> EMBEDS            = GROUP.listDependent("embeds",
                UniNode::asObjectNode,
                Embed.Binds.GROUP.autoConstructor(Embed.class, DiscordAPI.class),
                ArrayList::new
        );
        VarBind.TwoStage<String, String> EDITED_TIMESTAMP  = GROUP.bind2stage(
                "edited_timestamp",
                UniValueNode.ValueType.STRING,
                Function.identity()
        ); // todo Instant parsing
        VarBind.DependentTwoStage<UniObjectNode, DiscordAPI, User> AUTHOR            = GROUP.bindDependent("author",
                User.Binds.GROUP.autoConstructor(User.class, DiscordAPI.class)
        );
        VarBind.OneStage<String> CONTENT           = GROUP.bind1stage("content",
                UniValueNode.ValueType.STRING
        );
        VarBind.DependentTwoStage<Long, DiscordAPI, Channel> CHANNEL           = GROUP.bindDependent(
                "channel_id",
                UniValueNode.ValueType.LONG,
                DiscordAPI::getChannelById
        );
        VarBind.OneStage<Long> ID                = GROUP.bind1stage("id",
                UniValueNode.ValueType.LONG
        );
        ArrayBind.DependentTwoStage<UniObjectNode, DiscordAPI, Role, Collection<Role>> MENTIONED_ROLES   = GROUP.listDependent(
                "mentioned_roles",
                Role.Binds.GROUP.autoConstructor(Role.class, DiscordAPI.class),
                ArrayList::new
        );
        ArrayBind.DependentTwoStage<UniObjectNode, DiscordAPI, User, Collection<User>> MENTIONED_USERS   = GROUP.listDependent(
                "mentions",
                User.Binds.GROUP.autoConstructor(User.class, DiscordAPI.class),
                ArrayList::new
        );
        VarBind.OneStage<Boolean> MENTIONS_EVERYONE = GROUP.bind1stage(
                "mention_everyone",
                UniValueNode.ValueType.BOOLEAN
        );
        VarBind.OneStage<Boolean> PINNED            = GROUP.bind1stage("pinned",
                UniValueNode.ValueType.BOOLEAN
        );
        ArrayBind.DependentTwoStage<UniObjectNode, DiscordAPI, Reaction, Collection<Reaction>> REACTIONS         = GROUP.listDependent(
                "reactions",
                UniNode::asObjectNode,
                DiscordAPI::parseReaction,
                ArrayList::new
        );
        VarBind.TwoStage<String, String> TIMESTAMP         = GROUP.bind2stage("timestamp",
                UniValueNode.ValueType.STRING,
                Function.identity()
        ); // todo Instant parsing
        VarBind.OneStage<Boolean> TTS               = GROUP.bind1stage("tts",
                UniValueNode.ValueType.BOOLEAN
        );
        VarBind.TwoStage<Integer, Type> TYPE              = GROUP.bind2stage("type",
                UniValueNode.ValueType.INTEGER,
                Type::new
        );
    }
}
