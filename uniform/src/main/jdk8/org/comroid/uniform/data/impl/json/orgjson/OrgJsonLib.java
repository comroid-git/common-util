package org.comroid.uniform.data.impl.json.orgjson;

import java.util.Collections;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.func.bi.Junction;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniObjectNode;

import org.json.JSONArray;
import org.json.JSONObject;

@ClassDependency({ "org.json.JSONObject", "org.json.JSONArray" })
public final class OrgJsonLib extends SeriLib<Object, JSONObject, JSONArray> {
    public static @Instance final OrgJsonLib orgJsonLib = new OrgJsonLib();

    private final static Junction<String, Object> parser = Junction.of(
            JSONObject::new, Object::toString);

    private OrgJsonLib() {
        super(
                parser, JSONObject.class, JSONArray.class, JSONObject::getJSONArray,
                JSONArray::length, node -> {
                    if (node instanceof JSONArray) return ((JSONArray) node).toList();
                    if (node instanceof JSONObject) return Collections.singletonList(node);

                    throw new AssertionError();
                }
        );
    }

    @Override
    public <MT> UniObjectNode<Object, JSONObject, MT> createUniObjectNode(
            JSONObject node
    ) {
        throw new UnsupportedOperationException("no class defined");
    }

    @Override
    public <CT> UniArrayNode<Object, JSONArray, CT> createUniArrayNode(JSONArray node) {
        throw new UnsupportedOperationException("no class defined");
    }
}
