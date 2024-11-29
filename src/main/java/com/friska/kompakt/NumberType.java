package com.friska.kompakt;

/**
 * Represents a number type for {@link JSONParser} to know which type of Java number the JSON number strings should be
 * parsed to.
 */
public enum NumberType {

    INT("Integer"),
    FLOAT("Float"),
    DOUBLE("Double"),

    /**
     * @see java.math.BigDecimal
     */
    BIGDECIMAL("BigDecimal");

    public final String typeName;
    NumberType(String typeName){
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
