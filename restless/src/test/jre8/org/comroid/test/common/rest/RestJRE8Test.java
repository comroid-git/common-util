package org.comroid.test.common.rest;

import java.net.MalformedURLException;
import java.net.URL;

import org.comroid.common.rest.REST;
import org.comroid.common.rest.adapter.OkHttp3Adapter;
import org.comroid.common.rest.io.JSONSerializerAdapter;
import org.comroid.test.model.NGinXFSNode;

import com.alibaba.fastjson.JSON;
import okhttp3.MediaType;
import org.junit.Before;
import org.junit.Test;

public class RestJRE8Test {
    private REST rest;

    @Before
    public void setUp() {
        this.rest = REST.get(new OkHttp3Adapter<>(
                JSONSerializerAdapter.create$FastJSON(NGinXFSNode.class),
                MediaType.parse("application/json"))
        );
    }
    
    @Test
    public void testClient() throws MalformedURLException {
        REST.Request<NGinXFSNode, JSON> request = rest.<NGinXFSNode, JSON>request(REST.Context.DUMMY)
                .setUrl(new URL("https://api.cdn.kaleidox.de/plugin/"))
                .build();
        
        request.exe
    }
}
