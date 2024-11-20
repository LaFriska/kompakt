package com.friska;

/**
 * This class represents a set of configurations and utility methods needed throughout the library. Subordinate options
 * such as ident sizes will also be available here to be possibly modified, and methods allowing the serialisation of
 * an array of json-serialisable objects are also implemented here.
 */
public class Kompakt {

    private static int JSON_INDENT_SIZE = 4;

    public static void setJsonIndentSize(int size) {
        JSON_INDENT_SIZE = size;
    }

    protected static int getJsonIndentSize(){
        return JSON_INDENT_SIZE;
    }

}
