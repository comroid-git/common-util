package org.comroid.uniform.data.impl.json.fastjson;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.func.bi.Junction;
import org.comroid.common.spellbind.Spellbind;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.model.BaseNodeMember;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniObjectNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

@ClassDependency({
                         "com.alibaba.fastjson.JSON",
                         "com.alibaba.fastjson.JSONObject",
                         "com.alibaba.fastjson.JSONArray"
                 })
public final class FastJSONLib extends SeriLib<JSON, JSONObject, JSONArray> {
    public static @Instance final FastJSONLib fastJsonLib = new FastJSONLib();

    private static final Junction<String, JSON> parser = Junction.of(str -> {
        if (JSON.isValidObject(str)) return JSON.parseObject(str);
        if (JSON.isValidArray(str)) return JSON.parseArray(str);

        throw new IllegalArgumentException("Could not parse JSON String " + str);
    }, JSONAware::toJSONString);

    private FastJSONLib() {
        super(
                parser,
                JSONObject.class,
                JSONArray.class,
                (jsonObject, key) -> jsonObject.getJSONArray(key),
                JSONArray::size,
                node -> {
                    if (node instanceof JSONArray) return ((JSONArray) node).toJavaList(JSON.class);
                    return Collections.singletonList(node);
                }
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UniObjectNode<JSON, JSONObject, T> createUniObjectNode(final JSONObject node) {
        return Spellbind.builder(UniObjectNode.class)
                        .coreObject(node)
                        .subImplement(node, Map.class)
                        .subImplement((BaseNodeMember<JSONObject>) () -> node, BaseNodeMember.class)
                        .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UniArrayNode<JSON, JSONArray, T> createUniArrayNode(final JSONArray node) {
        return Spellbind.builder(UniArrayNode.class)
                        .coreObject(node)
                        .subImplement(node, List.class)
                        .subImplement((BaseNodeMember<JSONArray>) () -> node, BaseNodeMember.class)
                        .build();
    }
}
