package org.comroid.test.common.rest;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.comroid.common.Polyfill;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.rest.REST;
import org.comroid.common.rest.adapter.data.json.DataConverter$FastJSON;
import org.comroid.common.rest.adapter.http.HttpAdapter$OkHttp3;
import org.comroid.test.model.NGinXFSNode;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJava8 {
    public final static URL testUrl = Polyfill.url("https://api.cdn.kaleidox.de/plugin", null);

    private REST<NGinXFSNode> rest;

    @Before
    public void setup() {
        rest = REST.getOrCreate(NGinXFSNode.class, new HttpAdapter$OkHttp3(), new DataConverter$FastJSON<>(
                PredicateDuo.any(),
                DataConverter$FastJSON.autoConverter(NGinXFSNode.class))
        );
    }

    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        final REST<NGinXFSNode>.Request request = rest.request(testUrl)
                .method(REST.Method.GET);

        request.execute();

        assertTrue(request.execute().isDone());

        assertNotNull(request.execute$body()
                .get(0, TimeUnit.SECONDS));
        assertTrue(request.execute$deserialize()
                .get(0, TimeUnit.SECONDS).size() >= 3);
        assertEquals(200, (int) request.execute$statusCode()
                .get(0, TimeUnit.SECONDS));

        assertTrue(request.execute$map(NGinXFSNode::getType)
                .get(0, TimeUnit.SECONDS)
                .stream()
                .allMatch("directory"::equals));
    }
}
