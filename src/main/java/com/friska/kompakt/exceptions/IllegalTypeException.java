package com.friska.kompakt.exceptions;

/**
 * Thrown when an erroneous conversion of an arbitrary {@link Object} to a specific type is made.
 */
public class IllegalTypeException extends RuntimeException{
    public IllegalTypeException(String msg){
        super("An attempt to process a type in the JSON serialisation or deserialisation process occurred. " + msg);
    }

}
