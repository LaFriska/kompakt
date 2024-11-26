package com.friska;

/**
 * This class provides all tools in order to deserialise any arbitrary JSON-string to an instance of {@link JSONObject}.
 */
public class JSONDeserialiser {

    private final String originalString;

    protected JSONDeserialiser(String jsonString){
        this.originalString = jsonString;
    }

    private JSONObject deserialise(){
        String code = deleteSurroundingWhitespace(originalString);
        return null; //TODO
    }

    public static JSONObject deserialise(String jsonString){
        return new JSONDeserialiser(jsonString).deserialise();
    }


    /**
     * Simple recursive function used to remove delete whitespace characters surrounding a string.
     * @param str input string.
     * @return str with surrounding white spaces removed.
     */
    private static String deleteSurroundingWhitespace(String str){
        if(str.isBlank()) return "";
        if(Character.isWhitespace(str.charAt(0)))
            return deleteSurroundingWhitespace(str.substring(1));
        if(Character.isWhitespace(str.charAt(str.length() - 1)))
            return deleteSurroundingWhitespace(str.substring(0, str.length() - 1));
        return str;
    }

}
