import com.friska.kompakt.JSONParser;
import com.friska.kompakt.NumberType;
import com.friska.kompakt.exceptions.IllegalTypeException;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.friska.kompakt.JSONParser.*;
import static com.friska.kompakt.NumberType.*;

/**
 * This class extensively tests {@link JSONParser#parseNumber(String, NumberType)} and its related
 * methods.
 */
public class NumberParserTest {

    /**
     * Test for {@link JSONParser#isDigits(String)}.
     */
    @Test
    public void testIsDigits(){
        assertFalse(isDigits(""));
        assertTrue(isDigits("2"));
        assertTrue(isDigits("0"));
        assertTrue(isDigits("0000"));
        assertTrue(isDigits("002000"));
        assertTrue(isDigits("205802840239"));
        assertFalse(isDigits("20580a2840239"));
        assertFalse(isDigits("205802840239a"));
        assertFalse(isDigits("a205802840239"));
        assertFalse(isDigits("a"));
        assertTrue(isDigits("02309237623803285092375603790697327630970929736037"));
        assertFalse(isDigits("02309237623803285092375603790697a327630970929736037"));
    }

    /**
     * Test for {@link JSONParser#isExponent(String)}.
     */
    @Test
    public void testIsExponent(){
        assertTrue(isExponent(""));

        assertFalse(isExponent("E"));
        assertFalse(isExponent("e"));
        assertFalse(isExponent("0"));
        assertFalse(isExponent("Ea"));
        assertFalse(isExponent("A-3242"));
        assertFalse(isExponent("a"));
        assertFalse(isExponent("E-"));
        assertFalse(isExponent("e+"));

        assertTrue(isExponent("E2"));
        assertTrue(isExponent("e2"));
        assertTrue(isExponent("e+2"));
        assertTrue(isExponent("E-2"));
        assertTrue(isExponent("E-0000225232"));
        assertTrue(isExponent("E0000225232"));
        assertTrue(isExponent("E+0000225232"));

        assertFalse(isExponent("E-0000-225232"));
        assertFalse(isExponent("E+0000225232e"));
        assertFalse(isExponent("E-0000225232-"));
        assertFalse(isExponent("-0000225232"));
    }

    /**
     * Test for {@link JSONParser#isFraction(String)}.
     */
    @Test
    public void testIsFraction(){
        assertTrue(isFraction(""));

        assertFalse(isFraction("."));
        assertFalse(isFraction(".9."));
        assertFalse(isFraction("9929"));
        assertFalse(isFraction(".."));
        assertFalse(isFraction("23."));
        assertFalse(isFraction("3."));
        assertFalse(isFraction(".0000000."));
        assertFalse(isFraction(".00000002E22"));
        assertFalse(isFraction(".+0000000"));
        assertFalse(isFraction("+.0000000"));

        assertTrue(isFraction(".0"));
        assertTrue(isFraction(".3"));
        assertTrue(isFraction(".22"));
        assertTrue(isFraction(".29482902"));
        assertTrue(isFraction(".29482902294829022948290229482902294829022948290229482902"));
    }

    /**
     * Test for {@link JSONParser#isInteger(String)}.
     */
    @Test
    public void testIsInteger(){
        assertFalse(isInteger(""));
        assertFalse(isInteger("2.2"));
        assertFalse(isInteger("01"));
        assertFalse(isInteger("0258238"));
        assertFalse(isInteger("-0258238"));
        assertFalse(isInteger("+0258238"));
        assertFalse(isInteger("+123"));
        assertFalse(isInteger("123.1"));
        assertFalse(isInteger("123.0"));
        assertFalse(isInteger("232354343E23"));

        assertTrue(isInteger("0"));
        assertTrue(isInteger("2"));
        assertTrue(isInteger("-0"));
        assertTrue(isInteger("0"));
        assertTrue(isInteger("232354343"));
        assertTrue(isInteger("1234567890"));
        assertTrue(isInteger("987654567898765432123456789098987654"));
    }

    /**
     * Tests JSON strings that does not represent a number.
     */
    @Test
    public void testInvalidNumbers(){
        testInvalid("");
        testInvalid("\"");
        testInvalid("abc");
        testInvalid("02");
        testInvalid("02.6");
        testInvalid("02423224.6E+23");
        testInvalid("25802.23242.232");
        testInvalid("252.5E2e3");
        testInvalid("999.999999999999.");
        testInvalid("E");
        testInvalid("e2");
        testInvalid("E+23");
        testInvalid(".96");
        testInvalid("98.");
        testInvalid("91.2E");
        testInvalid("92.4e");
        testInvalid("true");
        testInvalid("false");
        testInvalid("252325.356434743E++2324");
        testInvalid("252325.356434743E+-2324");
        testInvalid("252325.356434743E+2324+");
        testInvalid("252325.356434743E_2324");
        testInvalid("252325356434743E232.4");
        testInvalid("-252325.356434743E++2324");
        testInvalid("-252325.356434743E+-2324");
        testInvalid("-252325.356434743E+2324+");
        testInvalid("-252325.356434743E_2324");
        testInvalid("-252325356434743E232.4");
    }

    /**
     * Tests valid numbers, but may not necessarily be compatible with each type.
     */
    @Test
    public void testValid(){
        testValid("null");
        testValid("3");
        testValid("2523252");
        testValid("25232.2352");
        testValid("25232.2352E+23252");
        testValid("25232.2352E252");
        testValid("-918367.2e-252");
        testValid("-918367.2e-252353643");
        testValid("2E42");
        testValid("23e-4");
        testValid("23e-4938593439");
        testValid("-0.000000000000000005e+23");
        testValid("-0.00000000000000000000000000000e0");
    }

    /**
     * Tests valid numbers but with erroneous types.
     */
    @Test
    public void testBadType(){
        badType("2.4", INT);
        badType("0.6", INT);
        badType("23532.4", INT);
        badType("208530802840583085028502808528402039203", INT);
        badType("2E23", INT);
        badType("-2147483649", INT);
        badType("2147483649", INT);
    }

    /**
     * Tests parsing to int.
     */
    @Test
    public void testIntegers(){
        testInt(23, "23");
        testInt(230, "230");
        testInt(Integer.MIN_VALUE, "-2147483648");
        testInt(Integer.MAX_VALUE, "2147483647");
        testInt(null, "null");
    }

    private void testInt(Integer expected, @NotNull String num){
        assertEquals(expected, parseNumber(num, INT));
    }

    private void testInvalid(@NotNull String num){
        assertThrows(IllegalArgumentException.class, () -> parseNumber(num, INT));
        assertThrows(IllegalArgumentException.class, () -> parseNumber(num, FLOAT));
        assertThrows(IllegalArgumentException.class, () -> parseNumber(num, DOUBLE));
        assertThrows(IllegalArgumentException.class, () -> parseNumber(num, BIGDECIMAL));
    }

    private void testValid(@NotNull String num){
        try {
            parseNumber(num, INT);
            parseNumber(num, FLOAT);
            parseNumber(num, DOUBLE);
            parseNumber(num, BIGDECIMAL);
        }catch (IllegalArgumentException e){
            fail();
        }catch (IllegalTypeException ignored){}
    }

    private void badType(@NotNull String num, NumberType type){
        assertThrows(IllegalTypeException.class, () -> parseNumber(num, type));
    }

}

