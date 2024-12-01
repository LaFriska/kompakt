package com.friska.kompakt.annotations;

import com.friska.kompakt.JSONSerialisable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to label a field variable of a class implementing {@link JSONSerialisable} as one that should
 * be serialised as a string. All fields labeled with this annotation will be serialised as a string type in the resulting
 * JSON, by calling the {@link Object#toString()} method.
 * @see JSONSerialisable
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerialiseAsString {}
