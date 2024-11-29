package com.friska.kompakt.exceptions;

import com.friska.kompakt.Attribute;
import com.friska.kompakt.JSONObject;

/**
 * Thrown when an attempt to access a non-existing {@link Attribute} from a {@link JSONObject} is made.
 */
public class AttributeNotFoundException extends RuntimeException{
    public AttributeNotFoundException(String name){
        super("Unable to retrieve attribute called \"" + name + "\".");
    }

}
