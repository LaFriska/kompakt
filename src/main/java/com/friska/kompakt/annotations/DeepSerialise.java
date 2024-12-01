package com.friska.kompakt.annotations;

import com.friska.kompakt.JSONSerialisable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes which implements {@link JSONSerialisable} labeled with this annotation will enable deep serialisation,
 * that is, inherited fields, including private ones will be included in the resulting JSON.
 * @see JSONSerialisable
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeepSerialise {}