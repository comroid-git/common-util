package org.comroid.common.rest.adapter.data.json;

import java.util.Collection;
import java.util.function.Function;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.common.rest.adapter.data.DataConverter;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataConverter$orgJSON<T> implements DataConverter<T, Object, JSONObject, JSONArray> {
    private final static Junction<String, Object> parser = Junction.of(JSONObject::new, Object::toString);

    public static <T> Junction<JSONObject, T> autoConverter(Class<T> forClass, Function<JSONObject, T> initializer) {
        return Junction.of(initializer, JSONObject::new);
    }

    private final PredicateDuo<JSONObject, T> filter;
    private final Junction<JSONObject, T> converter;

    public DataConverter$orgJSON(PredicateDuo<JSONObject, T> filter, Junction<JSONObject, T> converter) {
        this.filter = filter;
        this.converter = converter;
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public Junction<String, Object> getParser() {
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
    public DataStructureType getStructureType(Object data) {
        if (data instanceof JSONObject)
            return DataStructureType.OBJECT;
        if (data instanceof JSONArray)
            return DataStructureType.ARRAY;

        throw new IllegalArgumentException("Illegal argument type class: " + data.getClass().getName());
    }

    @Override
    public Collection<Object> split(JSONArray data) {
        final Span<Object> yields = new Span<>(data.length(), Span.NullPolicy.PROHIBIT, false);

        data.forEach(yields::add);

        return yields;
    }

    @Override
    public JSONArray combine(Span<Object> data) {
        final JSONArray jsonArray = new JSONArray();

        data.forEach(jsonArray::put);

        return jsonArray;
    }
}
