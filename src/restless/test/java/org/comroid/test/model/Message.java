package org.comroid.test.model;

import org.comroid.api.Polyfill;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainerBase;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import static org.comroid.test.FastJSONLib.fastJsonLib;


@Location(Message.Binds.class)
public final class Message extends DataContainerBase<DiscordAPI> {
    // get for non-optional field
    public final long getID() {
        return requireNonNull(Binds.ID);
    }

    // getter for optional field
    public final Optional<String> getEditedTimestamp() {
        // if wrapVar is not used, getVar might return null
        return wrap(Binds.EDITED_TIMESTAMP);
    }

    public Message(DiscordAPI api, UniObjectNode node) {
        super(node, api);
    }

    public interface Binds {
        @RootBind
        GroupBind<Message, DiscordAPI> GROUP = new GroupBind<>(fastJsonLib, "message", Message.class);
        VarBind<String, DiscordAPI, URL, ArrayList<URL>> ATTACHMENTS
                = GROUP.createBind("attachments")
                .extractAs(ValueType.STRING)
                .andRemap(Polyfill::url)
                .intoCollection(ArrayList::new)
                .build();
        VarBind<UniObjectNode, DiscordAPI, Embed, ArrayList<Embed>> EMBEDS
                = GROUP.createBind("embeds")
                .extractAsObject()
                .andConstruct(Embed.Binds.GROUP)
                .intoCollection(ArrayList::new)
                .build();
        VarBind<String, DiscordAPI, String, String> EDITED_TIMESTAMP
                = GROUP.createBind("edited_timestamp")
                .extractAs(ValueType.STRING)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<UniObjectNode, DiscordAPI, User, User> AUTHOR
                = GROUP.createBind("author")
                .extractAsObject()
                .andConstruct(User.Binds.GROUP)
                .onceEach()
                .build();
        VarBind<String, DiscordAPI, String, String> CONTENT
                = GROUP.createBind("content")
                .extractAs(ValueType.STRING)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<Long, DiscordAPI, Channel, Channel> CHANNEL
                = GROUP.createBind("channel_id")
                .extractAs(ValueType.LONG)
                .andResolve((id, api) -> api.getChannelById(id))
                .onceEach()
                .build();
        VarBind<Long, DiscordAPI, Long, Long> ID
                = GROUP.createBind("id")
                .extractAs(ValueType.LONG)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<UniObjectNode, DiscordAPI, Role, ArrayList<Role>> MENTIONED_ROLES
                = GROUP.createBind("mentioned_roles")
                .extractAsObject()
                .andConstruct(Role.Binds.GROUP)
                .intoCollection(ArrayList::new)
                .build();
        VarBind<UniObjectNode, DiscordAPI, User, ArrayList<User>> MENTIONED_USERS
                = GROUP.createBind("mentions")
                .extractAsObject()
                .andConstruct(User.Binds.GROUP)
                .intoCollection(ArrayList::new)
                .build();
        VarBind<Boolean, DiscordAPI, Boolean, Boolean> MENTIONS_EVERYONE
                = GROUP.createBind("mention_everyone")
                .extractAs(ValueType.BOOLEAN)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<Boolean, DiscordAPI, Boolean, Boolean> PINNED
                = GROUP.createBind("pinned")
                .extractAs(ValueType.BOOLEAN)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<UniObjectNode, DiscordAPI, Reaction, ArrayList<Reaction>> REACTIONS
                = GROUP.createBind("reactions")
                .extractAsObject()
                .andResolve((data, api) -> api.parseReaction(data))
                .intoCollection(ArrayList::new)
                .build();
        VarBind<String, DiscordAPI, String, String> TIMESTAMP
                = GROUP.createBind("timestamp")
                .extractAs(ValueType.STRING)
                .asIdentities()
                .onceEach()
                .build(); // todo Instant parsing
        VarBind<Boolean, DiscordAPI, Boolean, Boolean> TTS
                = GROUP.createBind("tts")
                .extractAs(ValueType.BOOLEAN)
                .asIdentities()
                .onceEach()
                .build();
        VarBind<Integer, DiscordAPI, Type, Type> TYPE
                = GROUP.createBind("type")
                .extractAs(ValueType.INTEGER)
                .andRemap(Type::new)
                .onceEach()
                .build();
    }

    public static class Type {
        public final int value;

        private Type(int value) {
            this.value = value;
        }
    }
}
