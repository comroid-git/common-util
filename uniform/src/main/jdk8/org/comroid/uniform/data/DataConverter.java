package org.comroid.uniform.data;

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
public abstract class DataConverter<TAR, BAS, OBJ extends BAS, ARR extends BAS> {
    public static <IN_T, OT_T> OT_T convert(
            DataConverter<?, IN_T, ? extends IN_T, ? extends IN_T> fromLib,
            DataConverter<?, OT_T, ? extends OT_T, ? extends OT_T> intoLib,
            IN_T node
    ) {
        return intoLib.getParser()
                .forward(fromLib
                        .getParser()
                        .backward(node));
    }

    private final String mimeType;

    protected DataConverter(String mimeType) {
        this.mimeType = mimeType;
    }

    public abstract Junction<String, BAS> getParser();

    public abstract PredicateDuo<OBJ, TAR> getFilter();

    public abstract Junction<OBJ, TAR> getConverter();

    public abstract DataStructureType getStructureType(BAS data);

    public abstract Collection<BAS> split(ARR data);

    public abstract ARR combine(Span<BAS> data);

    public Span<TAR> deserialize(String data) {
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
                elements = (Span<OBJ>) new Span<>(split(((ARR) node)));

                break;
        }

        return elements.stream()
                .map(getConverter()::forward)
                .collect(Span.collector());
    }

    public String serialize(Span<TAR> data) {
        final DataStructureType nodeType = data.isSingle() ? DataStructureType.OBJECT : DataStructureType.ARRAY;

        Span<OBJ> elements = null;

        switch (nodeType) {
            case OBJECT:
                final TAR tar = data.get();

                assert tar != null;
                elements = new Span<>(getConverter().backward(tar));

                break;
            case ARRAY:
                elements = data.stream()
                        .map(getConverter()::backward)
                        .collect(Span.collector());

                break;
        }

        //noinspection unchecked -> false positive
        return getParser().backward(combine((Span<BAS>) elements));
    }

    public final String getMimeType() {
        return mimeType;
    }

    public enum DataStructureType {
        OBJECT,
        ARRAY
    }
}
