package org.comroid.uniform.data.impl.json.orgjson;

import java.util.Collections;
import java.util.function.BiFunction;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.func.bi.Junction;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.StructureTypeMismatchException;

import org.json.JSONArray;
import org.json.JSONObject;

@ClassDependency({"org.json.JSONObject", "org.json.JSONArray"})
public final class OrgJsonLib extends SeriLib<Object, JSONObject, JSONArray> {
    public static @Instance final OrgJsonLib orgJsonLib = new OrgJsonLib();

    private final static Junction<String, Object> parser = Junction.of(JSONObject::new, Object::toString);

    private OrgJsonLib() {
        super(parser, JSONObject.class, JSONArray.class, JSONObject::getJSONArray, JSONArray::length, node -> {
            if (node instanceof JSONArray)
                return ((JSONArray) node).toList();
            if (node instanceof JSONObject)
                return Collections.singletonList(node);

            throw new AssertionError();
        });

        ReflectionHelper.verifyClassDependencies(OrgJsonLib.class);
    }

    @Override
    protected <TAR> NodeDummy<SeriLib<Object, JSONObject, JSONArray>, Object, JSONObject, JSONArray, TAR> createNodeDummy(TAR node) {
        return new NodeDummy<SeriLib<Object, JSONObject, JSONArray>, Object, JSONObject, JSONArray, TAR>(this, node) {
            @Override
            public boolean containsKey(String name) {
                return process(obj -> obj.has(name), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }

            @Override
            public <T> T getValueAs(String fieldName, Class<T> targetType) {
                return process(obj -> (T) obj.get(fieldName), arr -> {
                    // todo
                });
            }
        };
    }
}
