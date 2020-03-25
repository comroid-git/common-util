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
    Message message;

    @Before
    public void setup() {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemResourceAsStream("dummy/message.json")));

        JSONObject data = JSON.parseObject(reader.lines()
                                                 .collect(Collectors.joining()));

        message = new Message(new DiscordAPI(), data);
    }

    @Test
    public void testGetters() {
        assertEquals(1, message.getVar(Message.Binds.REACTIONS)
                               .size());
        assertFalse(message.getVar(Message.Binds.TTS));
        assertFalse(message.getVar(Message.Binds.TIMESTAMP)
                           .isEmpty());
        assertFalse(message.getVar(Message.Binds.MENTIONS_EVERYONE));
        assertEquals(334385199974967042L, message.getID());
        assertFalse(message.getVar(Message.Binds.PINNED));
        assertEquals(0, message.getVar(Message.Binds.EMBEDS)
                               .size());
        assertEquals(0, message.getVar(Message.Binds.ATTACHMENTS)
                               .size());
        assertFalse(message.getEditedTimestamp()
                           .isPresent());
        assertNotNull(message.getVar(Message.Binds.AUTHOR));
        assertEquals(0, message.getVar(Message.Binds.MENTIONED_ROLES)
                               .size());
        assertEquals("Supa Hot", message.getVar(Message.Binds.CONTENT));
        assertEquals(290926798999357250L, message.getVar(Message.Binds.CHANNEL).id);
        assertEquals(0, message.getVar(Message.Binds.MENTIONED_USERS)
                               .size());
        assertEquals(0, message.getVar(Message.Binds.TYPE).value);
    }
}
