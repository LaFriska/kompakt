package com.friska;

public class AttributeTypeException extends RuntimeException{ //TODO javadoc

    public AttributeTypeException(String msg){
        super("An attempt to fetch an attribute with an incorrect type occurred. " + msg);
    }

}
