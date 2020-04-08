package org.comroid.test.uniform;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.test.model.NGinXFSNode;
import org.comroid.uniform.adapter.data.json.fastjson.FastJsonDataConverter;
import org.comroid.uniform.http.REST;
import org.comroid.uniform.adapter.http.jdk.v11.JavaHttpAdapter;

import org.junit.Before;

import static org.comroid.common.Polyfill.url;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRestJava11 {
    public final static URL testUrl = url("https://api.cdn.comroid.org/app/SymBLink/");

    private REST<NGinXFSNode> rest;

    @Before
    public void setup() {
        rest = REST.getOrCreate(
                NGinXFSNode.class, new JavaHttpAdapter(),
                new FastJsonDataConverter<>(PredicateDuo.any(),
                        FastJsonDataConverter.autoConverter(NGinXFSNode.class)
                )
        );
    }

    @Before
    public void test() {
        final REST<NGinXFSNode>.Request request = rest.request(testUrl)
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
                    .size() >= 3);
            assertEquals(
                    200, (int) request.execute$statusCode()
                            .get(0, TimeUnit.SECONDS));

            assertTrue(request.execute$map(NGinXFSNode::getType)
                    .get(0, TimeUnit.SECONDS)
                    .stream()
                    .allMatch("directory"::equals));
        } catch (Throwable t) {
            System.out.println("Could not finish Java 11 Test");
            t.printStackTrace(System.out);
        }
    }
}
