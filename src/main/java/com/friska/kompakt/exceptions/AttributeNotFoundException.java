package com.friska.kompakt.exceptions;

public class AttributeNotFoundException extends RuntimeException{ //TODO javadoc

    public AttributeNotFoundException(String name){
        super("Unable to retrieve attribute called \"" + name + "\".");
    }

}
