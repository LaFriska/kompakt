package com.friska;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

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

    private static final HashSet<Character> HEX_DIGITS;

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

        HEX_DIGITS = new HashSet<>();
        HEX_DIGITS.add('1');
        HEX_DIGITS.add('2');
        HEX_DIGITS.add('3');
        HEX_DIGITS.add('4');
        HEX_DIGITS.add('5');
        HEX_DIGITS.add('6');
        HEX_DIGITS.add('7');
        HEX_DIGITS.add('8');
        HEX_DIGITS.add('9');
        HEX_DIGITS.add('0');
        HEX_DIGITS.add('a');
        HEX_DIGITS.add('b');
        HEX_DIGITS.add('c');
        HEX_DIGITS.add('d');
        HEX_DIGITS.add('e');
        HEX_DIGITS.add('f');
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
                Character val = safeFetch(value, i);
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

    private static Character toChar(@NotNull String hex){
        try{
            int c = Integer.parseInt(hex);
            return (char) c;
        }catch (NumberFormatException e){
            return null;
        }
    }

    private static Character safeFetch(String value, int index){
        if(index >= value.length() - 1 || index < 0) return null;
        char c = value.charAt(index);
        if(ESCAPE_CHARS.containsKey(c)){
            return ESCAPE_CHARS.get(c);
        }
        return null;
    }

}
