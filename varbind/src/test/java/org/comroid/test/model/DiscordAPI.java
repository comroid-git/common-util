package org.comroid.test.model;

import com.alibaba.fastjson.JSONObject;

public class DiscordAPI {
    public Channel getChannelById(long val) {
        return new Channel(val);
    }

    public User parseUser(JSONObject data) {
        return new User(this, data);
    }

    public Reaction parseReaction(JSONObject node) {
        return new Reaction(this, node);
    }
}
