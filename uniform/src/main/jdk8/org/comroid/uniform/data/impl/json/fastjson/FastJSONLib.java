package org.comroid.uniform.data.impl.json.fastjson;

import org.comroid.uniform.data.SeriLib;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class FastJSONLib extends SeriLib<JSON, JSONObject, JSONArray> {
    public static final FastJSONLib instance;

    static {
        try {
            Class.forName("com.alibaba.fastjson.JSON");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot initialize FastJSONLib: Missing dependency class", e);
        } finally {
            instance = new FastJSONLib();
        }
    }

    private FastJSONLib() {
        super(JSONObject.class, JSONArray.class);
    }
}
