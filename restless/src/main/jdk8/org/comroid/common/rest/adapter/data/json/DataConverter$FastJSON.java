package org.comroid.common.rest.adapter.data.json;

import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.common.rest.adapter.data.DataConverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class DataConverter$FastJSON<T> implements DataConverter<T, JSON, JSONObject, JSONArray> {
    private static final Junction<String, JSON> parser = Junction.of(str -> JSON.isValidObject(str) ? JSON.parseObject(str) : JSON.parseArray(str), JSONAware::toJSONString);

    public static <T> Junction<JSONObject, T> autoConverter(Class<T> forClass) {
        return new Junction<JSONObject, T>() {
            private final Class<T> target = forClass;

            @Override
            public T forward(JSONObject data) {
                return data.toJavaObject(target);
            }

            @Override
            public JSONObject backward(T object) {
                return (JSONObject) JSON.toJSON(object);
            }
        };
    }

    private final PredicateDuo<JSONObject, T> filter;
    private final Junction<JSONObject, T> converter;

    public DataConverter$FastJSON(PredicateDuo<JSONObject, T> filter, Junction<JSONObject, T> converter) {
        this.filter = filter;
        this.converter = converter;
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public Junction<String, JSON> getParser() {
        return parser;
    }

    @Override
    public PredicateDuo<JSONObject, T> getFilter() {
        return filter;
    }

    @Override
    public Junction<JSONObject, T> getConverter() {
        return converter;
    }

    @Override
    public DataStructureType getStructureType(JSON data) {
        return data instanceof JSONObject ? DataStructureType.OBJECT : DataStructureType.ARRAY;
    }

    @Override
    public Collection<JSONObject> split(JSONArray data) {
        return data.toJavaList(JSONObject.class);
    }

    @Override
    public JSONArray combine(Span<JSONObject> data) {
        final JSONArray jsonArray = new JSONArray();

        jsonArray.addAll(data);

        return jsonArray;
    }
}
