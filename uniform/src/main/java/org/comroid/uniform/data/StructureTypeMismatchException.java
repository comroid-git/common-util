package org.comroid.uniform.data;

public class StructureTypeMismatchException extends IllegalArgumentException {
    public StructureTypeMismatchException(DataStructureType<?, ?, ?> errorType, DataStructureType<?, ?, ?> expected) {
        super(String.format("Illegal Structure type %s; %s expected", errorType.typ, expected.typ));
    }
}
