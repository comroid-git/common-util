package org.comroid.uniform.data.impl.json.fastjson;

import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataConverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class DataConverter$FastJSON<T> extends DataConverter<T, JSON, JSONObject, JSONArray> {
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
        super(FastJSONLib.instance, "application/json");

        this.filter = filter;
        this.converter = converter;
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
    public Collection<JSON> split(JSONArray data) {
        return data.toJavaList(JSON.class);
    }

    @Override
    public JSONArray combine(Span<JSON> data) {
        final JSONArray jsonArray = new JSONArray();

        jsonArray.addAll(data);

        return jsonArray;
    }
}
