package org.comroid.test.model;

import com.alibaba.fastjson.JSONObject;
import org.comroid.uniform.node.UniObjectNode;

public class DiscordAPI {
    public Channel getChannelById(long val) {
        return new Channel(val);
    }

    public User parseUser(JSONObject data) {
        return new User(this, data);
    }

    public Reaction parseReaction(UniObjectNode node) {
        return new Reaction(this, node);
    }
}
