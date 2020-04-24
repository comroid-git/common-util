package org.comroid.test.uniform;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.comroid.restless.REST;
import org.comroid.restless.adapter.okhttp.v3.OkHttp3Adapter;
import org.comroid.test.model.NGinXFSNode;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;

import org.junit.Before;
import org.junit.Test;

import static org.comroid.common.Polyfill.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRestJava8 {
    public final static URL testUrl = url("https://api.cdn.comroid.org/app/SymBLink/");

    @Before
    public void setup() {
        rest = new REST<>(new OkHttp3Adapter(), FastJSONLib.fastJsonLib, null);
    }

    @Test
    public void test() {
        final REST<Void>.Request<NGinXFSNode> request = rest.request(NGinXFSNode.class)
                .method(REST.Method.GET);

        try {
            request.execute()
                    .join();

            assertTrue(request.execute()
                    .isDone());

            assertNotNull(request.execute$body()
                    .get(0, TimeUnit.SECONDS));
            assertTrue(request.execute$deserialize()
                    .get(0, TimeUnit.SECONDS)
                    .size() >= 1);
            assertEquals(
                    200,
                    (int) request.execute$statusCode()
                            .get(0, TimeUnit.SECONDS)
            );

            assertTrue(request.execute$map(NGinXFSNode::getType)
                    .get(0, TimeUnit.SECONDS)
                    .stream()
                    .allMatch("directory"::equals));
        } catch (Throwable t) {
            System.out.println("Could not finish Java 8 Test");
            t.printStackTrace(System.out);
        }
    }

    private REST rest;
}
