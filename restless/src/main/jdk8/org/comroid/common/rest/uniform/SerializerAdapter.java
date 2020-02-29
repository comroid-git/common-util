package org.comroid.common.rest.uniform;

public interface SerializerAdapter<DAT, TYP> {
    DAT parse(String data);

    TYP deserialize(DAT data);

    String serialize(TYP data);
}
