package org.comroid.uniform.adapter.data.json.fastjson;

import java.io.IOException;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;

import org.comroid.common.annotation.Instance;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import org.jetbrains.annotations.NotNull;

public final class FastJSONLib extends SerializationAdapter<JSON, JSONObject, JSONArray> {
    public static @Instance final FastJSONLib fastJsonLib = new FastJSONLib();

    private FastJSONLib() {
        super("application/json", JSONObject.class, JSONArray.class);
    }

    @Override
    public UniNode parse(String data) {
        final JSONValidator validator = JSONValidator.from(data);

        UniNode node = null;

        if (validator.validate()) {
            final JSONValidator.Type type = validator.getType();

            try {
                validator.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not close validator", e);
            }

            switch (type) {
                case Object:
                    node = createUniObjectNode(JSONObject.parseObject(data));
                    break;
                case Array:
                    node = createUniArrayNode(JSONArray.parseArray(data));
                    break;
                case Value:
                    throw new IllegalArgumentException("Cannot parse JSON Value");
            }
        } else throw new IllegalArgumentException("String is not valid JSON");

        return Objects.requireNonNull(node, "Node is null");
    }

    @Override
    public UniObjectNode createUniObjectNode(JSONObject node) {
        return new UniObjectNode(this, objectAdapter(node));
    }

    @Override
    public UniArrayNode createUniArrayNode(JSONArray node) {
        return new UniArrayNode(this, arrayAdapter(node));
    }

    private UniObjectNode.Adapter objectAdapter(JSONObject parseObject) {
        class Local extends AbstractMap<String, Object> implements UniObjectNode.Adapter {
            private final JSONObject baseNode = parseObject;

            @Override
            public Object getBaseNode() {
                return baseNode;
            }

            @Override
            public Object put(String key, Object value) {
                return baseNode.put(key, value);
            }

            @NotNull
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return baseNode.entrySet();
            }
        }

        return new Local();
    }

    private UniArrayNode.Adapter arrayAdapter(JSONArray parseObject) {
        class Local extends AbstractList<Object> implements UniArrayNode.Adapter {
            private final JSONArray baseNode = parseObject;

            @Override
            public Object getBaseNode() {
                return baseNode;
            }

            @Override
            public Object get(int index) {
                return baseNode.get(index);
            }

            @Override
            public Object set(int index, Object element) {
                return baseNode.set(index, element);
            }

            @Override
            public void add(int index, Object element) {
                baseNode.add(index, element);
            }

            @Override
            public Object remove(int index) {
                return baseNode.remove(index);
            }

            @Override
            public int size() {
                return baseNode.size();
            }
        }

        return new Local();
    }
}
