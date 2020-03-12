package org.comroid.uniform.data.impl.json.orgjson;

import org.comroid.uniform.data.SeriLib;

import org.json.JSONArray;
import org.json.JSONObject;

public final class OrgJsonLib extends SeriLib<Object, JSONObject, JSONArray> {
    public static final OrgJsonLib instance;

    static {
        try {
            Class.forName("org.json.JSONObject");
            Class.forName("org.json.JSONArray");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot initialize OrgJsonLib: Missing dependency class", e);
        } finally {
            instance = new OrgJsonLib();
        }
    }

    private OrgJsonLib() {
        super(JSONObject.class, JSONArray.class);
    }
}
