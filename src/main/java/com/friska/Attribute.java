package com.friska;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an arbitrary entry (field) into a JSON. All fields of a class being serialised to JSON is converted to
 * an instance of this record. Since attributes are uniquely defined by their name, the name parameter must never be
 * null.
 * @param name name of the attribute.
 * @param val value being stored.
 */
public record Attribute(@NotNull String name, @Nullable Object val) {

    /**
     * Equivalence is defined purely based on the name of two attributes, since attributes are always
     * uniquely defined by the name. This definition simplifies the process of detecting duplicate attributes.
     * @param obj   the reference object with which to compare.
     * @return whether this instance of Attribute equals the object.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj.equals(this)) return true;
        if(!(obj instanceof Attribute a)) return false;
        return a.name.equals(this.name);
    }
}
