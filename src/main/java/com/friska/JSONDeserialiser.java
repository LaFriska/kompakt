package com.friska;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class provides all tools in order to deserialise any arbitrary JSON-string to an instance of {@link JSONObject}.
 */
public class JSONDeserialiser {

    /**
     * The key set represents the set of every character that may follow an escape lateral in a JSON string.
     * The characters they associate to, are Java representations of this character, except from the unicode
     * character, in which case 'u' is associated.
     */
    private static final HashMap<Character, Character> ESCAPE_CHARS;

    private static final HashSet<Character> DIGITS;

    static{
        ESCAPE_CHARS = new HashMap<>();
        ESCAPE_CHARS.put('\"', '\"');
        ESCAPE_CHARS.put('\\', '\\');
        ESCAPE_CHARS.put('/', '/');
        ESCAPE_CHARS.put('b', '\b');
        ESCAPE_CHARS.put('f', '\f');
        ESCAPE_CHARS.put('n', '\n');
        ESCAPE_CHARS.put('r', '\r');
        ESCAPE_CHARS.put('t', '\t');
        ESCAPE_CHARS.put('u', 'u');

        DIGITS = new HashSet<>();
        DIGITS.addAll(List.of(new Character[]{'1','2','3','4','5','6','7','8','9','0'}));
    }

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

    public static Number parseNumber(@NotNull String value){
        return null; //TODO
    }

    private static boolean isJSONInteger(@NotNull String value){

        //Non-empty check
        if(value.isEmpty())
            return false;

        //Recursively ignore a minus sign.
        if(value.charAt(0) == '-')
            return isJSONInteger(value.substring(1));

        //If single character, return if it is a digit.
        if(value.length() == 1)
            return DIGITS.contains(value.charAt(0));

        //Otherwise 0 cannot be the leading character.
        if(value.charAt(0) == '0') return false;

        return isJSONDigits(value);
    }

    private static boolean isJSONFraction(@NotNull String value){
        if(value.isEmpty()) return true;
        if(value.charAt(0) != '.') return false;
        return isJSONDigits(value.substring(1));
    }

    private static boolean isJSONExponent(@NotNull String value){
        if(value.isEmpty()) return true;
        if(value.length() < 2) return false;
        if(value.charAt(0) != 'e' && value.charAt(0) != 'E') return false;
        if(isSign(value.charAt(1))){
            if(value.length() < 3) return false;
            return isJSONDigits(value.substring(2));
        }else{
            return isJSONDigits(value.substring(1));
        }
    }

    private static boolean isJSONDigits(@NotNull String value){
        if(value.isEmpty()) return false;
        for(int i = 0; i < value.length(); i++)
            if(!DIGITS.contains(value.charAt(i))) return false;
        return true;
    }

    private static boolean isSign(char c){
        return c == '+' || c == '-';
    }

    /**
     * Deserialises a JSON representation of a boolean, either "true" or "false" and encodes into a Java boolean value.
     * @param value string representation of the boolean.
     * @return a boolean represented by the string.
     * @throws IllegalArgumentException if the string value is not "true", "false", or "null".
     */
    public static Boolean parseBool(@NotNull String value){
        if(value.equals("true")) return true;
        if(value.equals("false")) return false;
        if(value.equals("null")) return null;
        throw new IllegalArgumentException("A JSON boolean value must either be \"true\", \"false\", or \"null\".");
    }

    /**
     * Deserialises a JSON representation of a string into a Java string. For this to work, the input must be syntactically correct.
     * That is, the input value is surrounded
     * by string laterals, and every other character is either a non-control character other than '"' or '\' (see {@link Character#isISOControl(char)}),
     * or it is an escape lateral '\' followed by an appropriate successor. By an "appropriate successor", we mean a
     * character or substrings of characters of the following list:
     * <ul>
     *     <li>
     *         Quotation Mark - '"'
     *     </li>
     *     <li>
     *         Reverse Solidus - '\'
     *     </li>
     *     <li>
     *         Solidus - '/'
     *     </li>
     *     <li>
     *         Backspace - 'b'
     *     </li>
     *     <li>
     *         Form Feed - 'f'
     *     </li>
     *     <li>
     *         Line Feed - 'n'
     *     </li>
     *     <li>
     *         Carriage Return - 'r'
     *     </li>
     *     <li>
     *         Character Tabulation - 't'
     *     </li>
     *     <li>
     *         Unicode - 'u' followed by 4 hexadecimal digits.
     *     </li>
     * </ul>
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>
     * @param value a JSON representation of a string.
     * @return A Java string of the JSON string.
     * @throws IllegalArgumentException if the input value is syntactically wrong.
     */
    public static String parseString(@NotNull String value){
        if(value.length() <= 1)
            throw new IllegalArgumentException("Representations of a JSON string must have length greater than 1.");
        if(value.equals("null")) return null;
        if(!value.startsWith("\"") || !value.endsWith("\""))
            throw new IllegalArgumentException("Representations of a JSON string must be wrapped with string laterals.");

        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < value.length() - 1; i++){
            char c = value.charAt(i);
            if(c == '\"')
                throw new IllegalArgumentException("Unexpected string lateral: '\"'.");
            else if(Character.isISOControl(c))
                throw new IllegalArgumentException("Unexpected control character.");
            else if(c == '\\'){
                Character val = safeFetch(value, i+1);
                if(val == null)
                    throw new IllegalArgumentException("Erroneous use of the escape lateral.");
                if(val.equals('u')){
                    String err = "Erroneous unicode character. " +
                            "Unicodes must be represented in the form \"\\uXXXX\", where" +
                            " \"XXXX\" is a substring of 4 hex digits.";
                    if(i + 5 >= value.length() - 1)
                        throw new IllegalArgumentException(err);
                    Character unicode = toChar(value.substring(i+2, i+6));
                    if(unicode == null)
                        throw new IllegalArgumentException(err);
                    sb.append(unicode);
                    i = i + 5;
                }else{
                    sb.append(val);
                    i++;
                }
            }else{
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Converts a string of hexadecimal digits to a character.
     * @param hex hex string.
     * @return A char represented by the unicode represented by the hex string, or null if the
     * string is formatted incorrectly.
     */
    private static Character toChar(@NotNull String hex){
        try{
            int c = Integer.parseInt(hex, 16);
            return (char) c;
        }catch (NumberFormatException e){
            return null;
        }
    }

    /**
     * Fetches a character from a string safely. If the index is out of bounded, returns null.
     */
    private static Character safeFetch(String str, int index){
        if(index >= str.length() - 1 || index < 0) return null;
        char c = str.charAt(index);
        if(ESCAPE_CHARS.containsKey(c)){
            return ESCAPE_CHARS.get(c);
        }
        return null;
    }

}
