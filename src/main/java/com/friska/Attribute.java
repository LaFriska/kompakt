package com.friska;

/**
 * Represents an arbitrary entry (field) into a JSON. All fields of a class being serialised to JSON is converted to
 * an instance of this record.
 * @param name name of the attribute.
 * @param val value being stored.
 */
public record Attribute(String name, Object val) {}
