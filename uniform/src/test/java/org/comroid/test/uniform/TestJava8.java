package org.comroid.test.uniform;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.test.model.NGinXFSNode;
import org.comroid.uniform.REST;
import org.comroid.uniform.data.impl.json.fastjson.FastJsonDataConverter;
import org.comroid.uniform.http.HttpAdapter$OkHttp3;

import org.junit.Before;
import org.junit.Test;

import static org.comroid.common.Polyfill.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJava8 {
    public final static URL testUrl = url("https://api.cdn.comroid.org/app/SymBLink/");

    private REST<NGinXFSNode> rest;

    @Before
    public void setup() {
        rest = REST.getOrCreate(NGinXFSNode.class, new HttpAdapter$OkHttp3(), new FastJsonDataConverter<>(
                PredicateDuo.any(),
                FastJsonDataConverter.autoConverter(NGinXFSNode.class))
        );
    }

    @Test
    public void test() {
        final REST<NGinXFSNode>.Request request = rest.request(testUrl)
                .method(REST.Method.GET);

        try {
            request.execute().join();

            assertTrue(request.execute().isDone());

            assertNotNull(request.execute$body()
                    .get(0, TimeUnit.SECONDS));
            assertTrue(request.execute$deserialize()
                    .get(0, TimeUnit.SECONDS).size() >= 1);
            assertEquals(200, (int) request.execute$statusCode()
                    .get(0, TimeUnit.SECONDS));

            assertTrue(request.execute$map(NGinXFSNode::getType)
                    .get(0, TimeUnit.SECONDS)
                    .stream()
                    .allMatch("directory"::equals));
        } catch (Throwable t) {
            System.out.println("Could not finish Java 8 Test");
            t.printStackTrace(System.out);
        }
    }
}
