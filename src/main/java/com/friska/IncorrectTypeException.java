package com.friska;

public class IncorrectTypeException extends RuntimeException{ //TODO javadoc

    public IncorrectTypeException(String msg){
        super("An attempt to fetch an attribute with an incorrect type occurred. " + msg);
    }

}
