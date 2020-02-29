package org.comroid.common.rest.uniform;

import java.util.Collection;

import org.comroid.common.iter.Span;

public interface SerializerAdapter<DAT, TYP, OBJ extends DAT, ARR extends DAT> {
    DAT parse(String data);

    default Span<TYP> deserialize(DAT data) {
        switch (type(data)) {
            case OBJECT:
                //noinspection unchecked
                return deserialize$object((OBJ) data);
            case ARRAY:
                return deserialize$array((ARR) data);
            default:
                throw new IllegalArgumentException("Cannot deserialize unknown object data type");
        }
    }

    Collection<TYP> deserialize$array(ARR data);
    
    TYP deserialize$object(OBJ data);

    String serialize(TYP data);
    
    DataType type(DAT data);
    
    enum DataType {
        OBJECT, ARRAY, UNKNOWN
    }
}
