package org.comroid.uniform.data.impl.json.orgjson;

import org.comroid.common.func.bi.Junction;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.StructureTypeMismatchException;

import org.json.JSONArray;
import org.json.JSONObject;

import static org.comroid.uniform.data.SeriLib.ClassDependency;

@ClassDependency({"org.json.JSONObject", "org.json.JSONArray"})
public final class OrgJsonLib extends SeriLib<Object, JSONObject, JSONArray> {
    public static final OrgJsonLib orgJsonLib = loadAdapter(OrgJsonLib.class);

    private final static Junction<String, Object> parser = Junction.of(JSONObject::new, Object::toString);

    protected OrgJsonLib() {
        super(parser, JSONObject.class, JSONArray.class);
    }

    @Override
    protected <TAR> NodeDummy<SeriLib<Object, JSONObject, JSONArray>, Object, JSONObject, JSONArray, TAR> createNodeDummy(TAR node) {
        return new NodeDummy<SeriLib<Object, JSONObject, JSONArray>, Object, JSONObject, JSONArray, TAR>(this, node, typeOf(node)) {
            @Override
            public boolean containsKey(String name) {
                return process(obj -> obj.has(name), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }
        };
    }
}
