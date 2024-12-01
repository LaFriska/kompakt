package com.friska.kompakt.annotations;

import com.friska.kompakt.JSONSerialisable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to ignore a field variable of a class implementing {@link JSONSerialisable}
 * from being serialised. The serialiser will actively omit any fields labeled with this annotation.
 * @see JSONSerialisable
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignored {}
