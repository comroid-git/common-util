package org.comroid.common.rest.io;

import java.util.Collection;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.common.rest.uniform.SerializerAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public final class JSONSerializerAdapter<DAT, TYP, OBJ extends DAT, ARR extends DAT> implements SerializerAdapter<DAT, TYP, OBJ, ARR> {
    public static <T> SerializerAdapter<JSON, T, JSONObject, JSONArray> create$FastJSON(final Class<T> type) {
        return new JSONSerializerAdapter<>(
                data -> {
                    switch (data.charAt(0)) {
                        case '{':
                            return JSON.parseObject(data);
                        case '[':
                            return JSON.parseArray(data);
                        default:
                            throw new IllegalArgumentException("Data is not a JSON Entity - data: " + data);
                    }
                },
                data -> data.toJavaObject(type),
                JSON::toJSONString
        );
    }

    private final Function<String, DAT> parser;
    private final Function<DAT, TYP> deserializer;
    private final Function<TYP, String> serializer;

    public JSONSerializerAdapter(Function<String, DAT> parser, Function<DAT, TYP> deserializer, Function<TYP, String> serializer) {
        this.parser = parser;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @Override
    public DAT parse(String data) {
        return parser.apply(data);
    }

    @Override
    public Span<TYP> deserialize(DAT data) {
        return deserializer.apply(data);
    }

    @Override public Collection<TYP> deserialize$array(ARR data) {
        return null;
    }

    @Override public TYP deserialize$object(OBJ data) {
        return null;
    }

    @Override
    public String serialize(TYP data) {
        return serializer.apply(data);
    }

    @Override public DataType type(DAT data) {
        return null;
    }
}
