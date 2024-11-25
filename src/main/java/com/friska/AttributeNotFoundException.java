package com.friska;

public class AttributeNotFoundException extends RuntimeException{ //TODO javadoc

    public AttributeNotFoundException(String name){
        super("Unable to retrieve attribute called \"" + name + "\".");
    }

}
