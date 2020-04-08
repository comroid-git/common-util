package org.comroid.uniform.adapter.data.json.orgjson;

import java.util.Collection;
import java.util.function.Function;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrgJsonDataConverter<T> extends DataConverter<T, Object, JSONObject, JSONArray> {
    public static <T> Junction<JSONObject, T> autoConverter(
            Class<T> forClass,
            Function<JSONObject, T> initializer
    ) {
        return Junction.of(initializer, JSONObject::new);
    }

    private final PredicateDuo<JSONObject, T> filter;
    private final Junction<JSONObject, T> converter;

    public OrgJsonDataConverter(
            PredicateDuo<JSONObject, T> filter,
            Junction<JSONObject, T> converter
    ) {
        super(OrgJsonLib.orgJsonLib, "application/json");

        this.filter = filter;
        this.converter = converter;
    }

    @Override
    public PredicateDuo<JSONObject, T> getFilter() {
        return filter;
    }

    @Override
    public Collection<Object> split(JSONArray data) {
        final Span<Object> yields = new Span<>();

        data.forEach(yields::add);

        return yields;
    }

    @Override
    public Junction<JSONObject, T> getConverter() {
        return converter;
    }

    @Override
    public JSONArray combine(Span<Object> data) {
        final JSONArray jsonArray = new JSONArray();

        data.forEach(jsonArray::put);

        return jsonArray;
    }
}
