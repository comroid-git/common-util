package org.comroid.test.common.rest;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.rest.REST;
import org.comroid.common.rest.adapter.data.json.DataConverter$FastJSON;
import org.comroid.common.rest.adapter.http.HttpAdapter$OkHttp3;
import org.comroid.test.model.NGinXFSNode;

import com.alibaba.fastjson.JSONObject;
import org.junit.Before;

public class TestJava8 {
    @Before
    public void setup() {
        rest = REST.getOrCreate(NGinXFSNode.class, new HttpAdapter$OkHttp3(), new DataConverter$FastJSON<>(
                PredicateDuo.any(),

        ))
    }

    @Before
    public void test() {
    }
}
