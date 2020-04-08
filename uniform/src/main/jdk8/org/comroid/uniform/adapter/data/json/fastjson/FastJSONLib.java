package org.comroid.uniform.adapter.data.json.fastjson;

import java.io.IOException;
import java.util.Objects;

import org.comroid.common.annotation.Instance;
import org.comroid.uniform.data.SerializationAdapter;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;

public final class FastJSONLib extends SerializationAdapter<JSON, JSONObject, JSONArray> {
    public static @Instance final FastJSONLib fastJsonLib = new FastJSONLib();

    private FastJSONLib() {
        super(JSONObject.class, JSONArray.class);
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
                    node = new UniObjectNode(this, objectAdapter(JSONObject.parseObject(data)));
                    break;
                case Array:
                    node = new UniArrayNode(this, arrayAdapter(JSONArray.parseArray(data)));
                    break;
                case Value:
                    throw new IllegalArgumentException("Cannot parse JSON Value");
            }
        } else throw new IllegalArgumentException("String is not valid JSON");

        return Objects.requireNonNull(node, "Node is null");
    }

    private UniObjectNode.Adapter objectAdapter(JSONObject parseObject) {
        class Local extends JSONObject implements UniObjectNode.Adapter {
            private final JSONObject baseNode = parseObject;

            @Override
            public Object getBaseNode() {
                return baseNode;
            }
        }

        return new Local();
    }

    private UniArrayNode.Adapter arrayAdapter(JSONArray parseObject) {
        class Local extends JSONArray implements UniArrayNode.Adapter {
            private final JSONArray baseNode = parseObject;

            @Override
            public Object getBaseNode() {
                return baseNode;
            }
        }

        return new Local();
    }

    @Override
    public UniObjectNode createUniObjectNode(JSONObject node) {
        return null;
    }

    @Override
    public UniArrayNode createUniArrayNode(JSONArray node) {
        return null;
    }
}
