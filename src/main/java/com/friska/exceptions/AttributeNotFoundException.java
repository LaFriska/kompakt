package com.friska.exceptions;

public class AttributeNotFoundException extends RuntimeException{ //TODO javadoc

    public AttributeNotFoundException(String name){
        super("Unable to retrieve attribute called \"" + name + "\".");
    }

}
