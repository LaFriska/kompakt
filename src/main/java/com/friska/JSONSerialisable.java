package com.friska;

/**
 * Classes implementing this interface allows the library to search through field variables and serialise them into a
 * JSON string. This process is done by calling {@link JSONSerialisable#serialise()}. The programme will interpret the
 * variables at its own discretion, for more control over the serialisation process, classes should instead extend
 * {@link JSONSerialiser}.
 */
public interface JSONSerialisable {

    default String serialise(){
        return serialise(this, Kompakt.getJsonIndentSize());
    }

    private static <T extends JSONSerialisable> String serialise(T obj, int indentSize){
        return null;
    }

}
