package org.harrel.java2ts;

public interface TsType {

    default String getNullableTypeName() {
        return getTypeName() + " | null";
    }

    String getTypeName();


}
