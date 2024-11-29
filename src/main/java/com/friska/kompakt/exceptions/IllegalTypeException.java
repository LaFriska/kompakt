package com.friska.kompakt.exceptions;

public class IllegalTypeException extends RuntimeException{ //TODO javadoc

    public IllegalTypeException(String msg){
        super("An attempt to process a type in the JSON serialisation or deserialisation process occurred. " + msg);
    }

}
