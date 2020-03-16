package org.comroid.uniform.data.impl.json.fastjson;

import org.comroid.common.func.bi.Junction;
import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.StructureTypeMismatchException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

import static org.comroid.uniform.data.SeriLib.ClassDependency;

@ClassDependency({"com.alibaba.fastjson.JSON", "com.alibaba.fastjson.JSONObject", "com.alibaba.fastjson.JSONArray"})
public final class FastJSONLib extends SeriLib<JSON, JSONObject, JSONArray> {
    public static final FastJSONLib fastJsonLib = loadAdapter(FastJSONLib.class);

    private static final Junction<String, JSON> parser = Junction.of(
            str -> {
                if (JSON.isValidObject(str))
                    return JSON.parseObject(str);
                if (JSON.isValidArray(str))
                    return JSON.parseArray(str);

                throw new IllegalArgumentException("Could not parse JSON String " + str);
            }, JSONAware::toJSONString
    );

    private FastJSONLib() {
        super(parser, JSONObject.class, JSONArray.class);
    }

    @Override
    protected <TAR extends JSON> NodeDummy<SeriLib<JSON, JSONObject, JSONArray>, JSON, JSONObject, JSONArray, TAR> createNodeDummy(TAR node) {
        return new NodeDummy<SeriLib<JSON, JSONObject, JSONArray>, JSON, JSONObject, JSONArray, TAR>(this, node, typeOf(node)) {
            @Override
            public boolean containsKey(String name) {
                return process(obj -> obj.containsKey(name), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }

            @Override
            public <T> T getValueAs(String fieldName, Class<T> targetType) {
                return process(obj -> obj.getObject(fieldName, targetType), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }
        };
    }
}
