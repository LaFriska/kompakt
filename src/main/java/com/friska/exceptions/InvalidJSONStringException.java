package com.friska.exceptions;

public class InvalidJSONStringException extends RuntimeException{
    public InvalidJSONStringException(String errorMessage, String jsonCode){
        super("A problem occurred trying to read a JSON-string.\n"
                + errorMessage + "\n" + "Please check for syntactic correctness in the below JSON-string:\n" + jsonCode);
    }

}
