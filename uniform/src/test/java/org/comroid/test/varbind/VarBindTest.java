package org.comroid.test.varbind;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.comroid.test.model.DiscordAPI;
import org.comroid.test.model.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class VarBindTest {
    @Before
    public void setup() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(
                "dummy/message.json")));

        JSONObject data = JSON.parseObject(reader.lines()
                .collect(Collectors.joining()));

        message = new Message(new DiscordAPI(), data);
    }

    @Test
    public void testGetters() {
        assertEquals(
                1,
                message.get(Message.Binds.REACTIONS)
                        .size()
        );
        assertFalse(message.get(Message.Binds.TTS));
        assertFalse(message.get(Message.Binds.TIMESTAMP)
                .isEmpty());
        assertFalse(message.get(Message.Binds.MENTIONS_EVERYONE));
        assertEquals(334385199974967042L, message.getID());
        assertFalse(message.get(Message.Binds.PINNED));
        assertEquals(
                0,
                message.get(Message.Binds.EMBEDS)
                        .size()
        );
        assertEquals(
                0,
                message.get(Message.Binds.ATTACHMENTS)
                        .size()
        );
        assertFalse(message.getEditedTimestamp()
                .isPresent());
        assertNotNull(message.get(Message.Binds.AUTHOR));
        assertEquals(
                0,
                message.get(Message.Binds.MENTIONED_ROLES)
                        .size()
        );
        assertEquals("Supa Hot", message.get(Message.Binds.CONTENT));
        assertEquals(290926798999357250L, message.get(Message.Binds.CHANNEL).id);
        assertEquals(
                0,
                message.get(Message.Binds.MENTIONED_USERS)
                        .size()
        );
        assertEquals(0, message.get(Message.Binds.TYPE).value);
    }
    Message message;
}
