package org.comroid.uniform.adapter.data.json.orgjson;

import org.comroid.common.annotation.Instance;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import org.json.JSONArray;
import org.json.JSONObject;

public final class OrgJsonLib extends SerializationAdapter<Object, JSONObject, JSONArray> {
    public static @Instance final OrgJsonLib orgJsonLib = new OrgJsonLib();

    protected OrgJsonLib() {
        super("application/json", JSONObject.class, JSONArray.class);
    }

    @Override
    public UniNode parse(String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniObjectNode createUniObjectNode(JSONObject node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniArrayNode createUniArrayNode(JSONArray node) {
        throw new UnsupportedOperationException();
    }
}
