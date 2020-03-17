package org.comroid.uniform.data;

import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;

import static org.comroid.uniform.data.DataStructureType.Primitive.ARRAY;
import static org.comroid.uniform.data.DataStructureType.Primitive.OBJECT;

/**
 * @param <TAR> The target Type.
 * @param <BAS> The basic deserialized type.
 * @param <OBJ> The object deserialization type.
 * @param <ARR> The array deserialization type.
 */
public abstract class DataConverter<TAR, BAS, OBJ extends BAS, ARR extends BAS> {
    public static <IN_T, OT_T> OT_T convert(
            SeriLib<IN_T, ? extends IN_T, ? extends IN_T> fromLib,
            SeriLib<OT_T, ? extends OT_T, ? extends OT_T> intoLib,
            IN_T node
    ) {
        return intoLib.parser
                .forward(fromLib.parser
                        .backward(node));
    }

    public final SeriLib<BAS, OBJ, ARR> seriLib;
    public final String mimeType;

    protected DataConverter(SeriLib<BAS, OBJ, ARR> seriLib, String mimeType) {
        this.seriLib = seriLib;
        this.mimeType = mimeType;
    }

    public abstract PredicateDuo<OBJ, TAR> getFilter();

    public abstract Junction<OBJ, TAR> getConverter();

    public abstract Collection<BAS> split(ARR data);

    public abstract ARR combine(Span<BAS> data);

    public Span<TAR> deserialize(String data) {
        final BAS node = seriLib.parser.forward(data);
        final DataStructureType.Primitive nodeType = seriLib.typeOf(node).typ;

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
                .collect(Span.collector(false));
    }

    public String serialize(Span<TAR> data) {
        final DataStructureType.Primitive nodeType = data.isSingle() ? OBJECT : ARRAY;

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
                        .collect(Span.collector(false));

                break;
        }

        //noinspection unchecked -> false positive
        return seriLib.parser.backward(combine((Span<BAS>) elements));
    }
}
