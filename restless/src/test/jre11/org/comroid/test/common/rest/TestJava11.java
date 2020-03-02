package org.comroid.test.common.rest;

import org.comroid.common.rest.REST;
import org.comroid.common.rest.adapter.data.json.DataAdapter$FastJSON;
import org.comroid.common.rest.adapter.http.HttpAdapter$JDK11;

import org.junit.Before;

public class TestJava11 {
    private REST rest;
    
    @Before
    public void setup() {
        rest = REST.newClient(HttpAdapter$JDK11.instance, DataAdapter$FastJSON.instance);
    }
    
    @Before
    public void test() {
    }
}
