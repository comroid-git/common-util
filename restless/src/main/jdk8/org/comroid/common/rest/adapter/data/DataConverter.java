package org.comroid.common.rest.adapter.data;

import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;

/**
 * @param <TAR> The target Type.
 * @param <BAS> The basic deserialized type.
 * @param <OBJ> The object deserialization type.
 * @param <ARR> The array deserialization type.
 */
public interface DataConverter<TAR, BAS, OBJ extends BAS, ARR extends BAS> {
    Junction<String, BAS> getParser();

    PredicateDuo<OBJ, TAR> getFilter();

    Junction<OBJ, TAR> getConverter();

    DataStructureType getStructureType(BAS data);

    Collection<OBJ> split(ARR data);

    ARR combine(Span<OBJ> data);

    default Span<TAR> deserialize(String data) {
        final BAS node = getParser().forward(data);
        final DataStructureType nodeType = getStructureType(node);

        Span<OBJ> elements = null;

        switch (nodeType) {
            case OBJECT:
                //noinspection unchecked
                elements = new Span<>(((OBJ) node));

                break;
            case ARRAY:
                //noinspection unchecked
                elements = new Span<>(split(((ARR) node)));

                break;
        }

        return elements.stream()
                .map(getConverter()::forward)
                .collect(Span.collector());
    }

    default String serialize(Span<TAR> data) {
        final DataStructureType nodeType = data.isSingle() ? DataStructureType.OBJECT : DataStructureType.ARRAY;

        Span<OBJ> elements = null;

        switch (nodeType) {
            case OBJECT:
                elements = new Span<>(getConverter().backward(data.get()));

                break;
            case ARRAY:
                elements = data.stream()
                        .map(getConverter()::backward)
                        .collect(Span.collector());

                break;
        }

        return getParser().backward(combine(elements));
    }

    enum DataStructureType {
        OBJECT,
        ARRAY
    }
}
