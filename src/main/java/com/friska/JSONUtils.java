package com.friska;

/**
 * This class holds a variety of settings and configurations for JSON serialisation, there are also utiliy methods
 * relating to the JSON standard for the programme to ensure every detail is correctly implemented.
 */
public class JSONUtils {

    /**
     * The size of indentation in the resulting JSON string.
     */
    protected static int INDENT_SIZE = 2;

    /**
     * Sanitises a string by escaping control characters, quotation marks, or dealing with control characters, such that
     * the given string in Java can be represented appropriate in JSON.
     * @param string input string.
     * @return a sanitised string compatible to be placed around string laterals and stored as a JSON string.
     */
    public static String sanitiseString(String string){
        if(string == null) return null;
        StringBuilder sb = new StringBuilder(string);
        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            switch (c){
                case '\\' -> {
                    sb.replace(i, i+1, "\\\\");
                    i++;
                }
                case '"' -> {
                    sb.replace(i, i+1, "\\\"");
                    i++;
                }
                case '\b' -> {
                    sb.replace(i, i+1, "\\b");
                    i++; continue;
                }
                case '\f' -> {
                    sb.replace(i, i+1, "\\f");
                    i++; continue;
                }
                case '\n' -> {
                    sb.replace(i, i+1, "\\n");
                    i++; continue;
                }
                case '\r' -> {
                    sb.replace(i, i+1, "\\r");
                    i++; continue;
                }
                case '\t' -> {
                    sb.replace(i, i+1, "\\t");
                    i++; continue;
                }
            }
            //If character is not of the above and is still a control character
            if(Character.isISOControl(c)){
                String unicode = "\\u" + String.format("%04X", (int) c);
                sb.replace(i, i+1, unicode);
                i = i + 5;
            }
        }
        return sb.toString();
    }
}
