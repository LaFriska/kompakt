package com.friska.kompakt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an arbitrary entry (field) into a JSON. All fields of a class being serialised to JSON is converted to
 * an instance of this record. Since attributes are uniquely defined by their name, the name parameter must never be
 * null. Equivalence on Attribute is also defined based on the equivalence between their names.
 *
 * @param name name of the attribute.
 * @param val  value being stored.
 * @param serialiseAsString whether this attribute should be serialised as a string when processed by
 * {@link JSONSerialisable}. In most circumstances this parameter can be ignored, and the constructor with just
 *                          its name and value as an input will by default set it to false.
 */
public record Attribute(@NotNull String name, @Nullable Object val, boolean serialiseAsString) {

    public Attribute(@NotNull String name, @Nullable Object val){
        this(name, val, false);
    }

    /**
     * Equivalence is defined purely based on the name of two attributes, since attributes are always
     * uniquely identified by the name. This definition simplifies the process of detecting duplicate attributes.
     *
     * @param obj the reference object with which to compare.
     * @return whether this instance of Attribute equals the object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.equals(this)) return true;
        if (!(obj instanceof Attribute a)) return false;
        return a.name.equals(this.name);
    }
}
