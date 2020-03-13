package org.comroid.test.model;

public class DiscordAPI {
    public Channel getChannelById(long val) {
        return new Channel(val);
    }
}
