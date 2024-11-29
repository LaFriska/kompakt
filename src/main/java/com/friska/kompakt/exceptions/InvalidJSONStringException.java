package com.friska.kompakt.exceptions;

public class InvalidJSONStringException extends RuntimeException{




    public InvalidJSONStringException(String errorMessage){
        super("A problem occurred trying to read a JSON-string.\n"
                + errorMessage);
    }

}
