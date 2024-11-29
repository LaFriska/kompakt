package com.friska;

import com.friska.exceptions.IllegalTypeException;
import com.friska.exceptions.InvalidJSONStringException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class provides all tools in order to deserialise any arbitrary JSON-string to an instance of {@link JSONObject}.
 */
public class JSONParser {

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
     * In JSON, a value is a segment of data either represented by the entire JSON string, or stored inside an object
     * or an array. A value is either an object, array, string, number, boolean, or null. This method takes a string
     * input representing a value and deserialises it into an arbitrary {@link Object} of the aforementioned types.
     * Since this method may require parsing objects and arrays, which are defined using the definition of a value, this
     * method is in mutual recursion with {@link JSONParser#parseObject(String)} and {@link JSONParser#parseArray(String)}.
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>
     * @param value string representation of the value.
     * @param type number type for number values or sub-values.
     * @return If the value represents an object, an instance of {@link JSONObject} is returned. If it represents an array, then
     *         an {@link Object} array is returned. For numbers, either one of the four children of {@link Number} represented
     *         by the enumerator {@link NumberType} is used as the return type.
     * @throws IllegalArgumentException if the input string does not represent a JSON value.
     * @throws IllegalTypeException if the input string represents a number but cannot be converted to the specified type.
     */
    public static Object parseValue(@NotNull String value, @NotNull NumberType type){
        if(value.isEmpty()) throw new IllegalArgumentException("Expected JSON value.");
        if(value.equals("null")) return null;
        if(value.startsWith("\"")) return parseString(value);
        if(value.startsWith("{")) return parseObject(value);
        if(value.startsWith("[")) return parseArray(value);
        try{
            return parseNumber(value, type);
        }catch(IllegalArgumentException ignored){}
        try{
            return parseBool(value);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Unexpected representation of a JSON value: " + value);
        }
    }

    public static JSONObject parseObject(@NotNull String value){
        return null; //TODO
    }

    public static Object[] parseArray(@NotNull String value){
        return null; //TODO
    }

    /**
     * In JSON, a member is a representation of a key-value pair: some number of whitespace surrounding a string,
     * then a colon, and a value surrounded by an arbitrary number of whitespace. This method takes a JSON-string
     * representation of a member and converts it to an {@link Attribute}.
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>.
     * @param value string representation of a member.
     * @return an attribute holding information on the key-value pair.
     * @throws IllegalArgumentException if the string is syntactically incorrect, or that the value of the key-value
     * pair is syntactically incorrect.
     */
    public static @NotNull Attribute parseMember(@NotNull String value, @NotNull NumberType type){
        String[] args = safeSplit(value, ':');
        if(args.length > 2) throw new IllegalArgumentException("Unexpected token ':' in member.");
        if(args.length < 2) throw new IllegalArgumentException("All members must have the form <String> : <Value>.");
        String name = parseString(args[0]);
        if(name == null) throw new IllegalArgumentException("Name of attribute cannot be null.");
        return new Attribute(name, parseValue(args[1], type));
    }

    /**
     * Splits the string into a string array, cutting it whenever a char is encountered. This method is also string-safe,
     * object-safe and array-safe
     * that is, if the char is wrapped inside JSON-strings, or sub-objects or arrays, then it should be ignored.
     * Aside from this additional criteria, this works in similar fashion to
     * {@link String#split(String)}.
     * @param value string to be split.
     * @param split the character that splits the string.
     * @return a string array representing the split.
     */
    public static String[] safeSplit(@NotNull String value, char split){

        boolean insideString = false;
        ArrayList<String> list = new ArrayList<>();

        int current = 0;
        int objDepth = 0;
        int arrayDepth = 0;

        for(int i = 0; i < value.length(); i++){
            char c = value.charAt(i);

            //Handles string laterals and escape laterals
            switch (c){
                case '\"' -> insideString = !insideString;
                case '\\' -> {if(insideString) i++;}
            }

            //Changes depth level of data structures being read.
            if(!insideString){
                switch (c){
                    case '}' -> objDepth = safeDec(objDepth);
                    case ']' -> arrayDepth = safeDec(arrayDepth);
                    case '{' -> objDepth++;
                    case '[' -> arrayDepth++;
                }
            }

            if(!insideString && c == split && objDepth == 0 && arrayDepth == 0){
                list.add(value.substring(current, i));
                current = i+1;
            }
        }
        list.add(value.substring(current));
        return list.toArray(new String[0]);
    }

    private static int safeDec(int val){
        return val <= 0 ? 0 : val-1;
    }


    /**
     * In JSON, a number is defined as an integer followed by a fraction and then an exponent. See the definition
     * of these terms in methods below, or refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>.
     * <p>
     * This method takes a string representation of a JSON number, and an instance of {@link NumberType}, and parses it
     * into a Java number.
     * @param value string representation of a JSON number.
     * @param type type of the number that should be return.
     * @return an instance of a particular implementation of {@link Number} representing value.
     * @throws IllegalArgumentException if value is not a JSON number.
     * @throws IllegalTypeException if the number cannot be converted to the specified type.
     */
    public static Number parseNumber(@NotNull String value, @NotNull NumberType type){

        if(value.equals("null")) return null;

        //Hurdles to check if value is a valid number
        String err = "Input string does not represent a JSON number.";
        int intEnd = value.length();
        for(int i = 0; i < value.length(); i++){
            if(value.charAt(i) == '.' || value.charAt(i) == 'e' || value.charAt(i) == 'E'){
                intEnd = i;
                break;
            }
        }
        if(!isInteger(value.substring(0, intEnd)))
            throw new IllegalArgumentException(err);

        if(!isExponent(value.substring(intEnd))) {
            int fracEnd = value.length();
            for (int i = intEnd; i < value.length(); i++) {
                if (value.charAt(i) == 'e' || value.charAt(i) == 'E') {
                    fracEnd = i;
                    break;
                }
            }
            if (!isFraction(value.substring(intEnd, fracEnd)))
                throw new IllegalArgumentException(err);
            if (!isExponent(value.substring(fracEnd)))
                throw new IllegalArgumentException(err);
        }
        try{
            Number res = switch(type){
                case INT -> Integer.parseInt(value);
                case FLOAT -> Float.parseFloat(value);
                case DOUBLE -> Double.parseDouble(value);
                case BIGDECIMAL -> new BigDecimal(value);
            };
            return res;
        }catch(NumberFormatException e){
            throw new IllegalTypeException("Number represented by " + value
                    + " cannot be converted to an instance of " + type + ".");
        }
    }

    /**
     * In JSON, an integer is defined as a single digit, or a single non-zero digit followed by a digit string, all of
     * which may or may not contain a negation '-' at the start.
     * @return whether an input string is an integer.
     */
    public static boolean isInteger(@NotNull String value){

        //Non-empty check
        if(value.isEmpty())
            return false;

        //Recursively ignore a minus sign.
        if(value.charAt(0) == '-')
            return isInteger(value.substring(1));

        //If single character, return if it is a digit.
        if(value.length() == 1)
            return DIGITS.contains(value.charAt(0));

        //Otherwise 0 cannot be the leading character.
        if(value.charAt(0) == '0') return false;

        return isDigits(value);
    }

    /**
     * In JSON, a "fraction" is defined as either an empty string, or '.' followed by a digits string.
     * @return whether an input string is a fraction.
     */
    public static boolean isFraction(@NotNull String value){
        if(value.isEmpty()) return true;
        if(value.charAt(0) != '.') return false;
        return isDigits(value.substring(1));
    }

    /**
     * In JSON, an exponent is defined as either an empty string, or 'E' (or 'e') followed by a sign and a digits string.
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>
     * @return whether an input string is an exponent.
     */
    public static boolean isExponent(@NotNull String value){
        if(value.isEmpty()) return true;
        if(value.length() < 2) return false;
        if(value.charAt(0) != 'e' && value.charAt(0) != 'E') return false;
        if(isSign(value.charAt(1))){
            if(value.length() < 3) return false;
            return isDigits(value.substring(2));
        }else{
            return isDigits(value.substring(1));
        }
    }

    /**
     * A "digits" string is simply defined as a string where each character is a decimal digit.
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>
     * @return whether an input string is a digits string.
     */
    public static boolean isDigits(@NotNull String value){
        if(value.isEmpty()) return false;
        for(int i = 0; i < value.length(); i++)
            if(!DIGITS.contains(value.charAt(i))) return false;
        return true;
    }

    /**
     * A sign character in JSON is defined as either '+' or '-'.
     * For more information, please refer to <a href="https://www.json.org/json-en.html">the JSON documentations.</a>.
     * @return whether an input char is a sign.
     */
    public static boolean isSign(char c){
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
