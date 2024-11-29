import com.friska.kompakt.JSONUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.friska.kompakt.JSONUtils.sanitiseString;

/**
 * This class tests the method {@link JSONUtils#sanitiseString(String)}.
 */
public class SanitiseStringTest {

    /**
     * "Normal" strings should not be modified.
     */
    @Test
    public void testTrivial(){
        assertIdentity(null);
        assertIdentity("Hello World!");
        assertIdentity("Hello ' World ///");
        assertIdentity("abcdefghijklmnopqrstuvwxyz!@#$%^&*()0987654321...,,,");
        assertIdentity("");
    }

    private void assertIdentity(String str){
        assertEquals(str, sanitiseString(str));
    }

    /**
     * Tests sanitization of escape laterals and escaping quotations.
     */
    @Test
    public void testEscape(){
        test("\\\\", "\\");
        test("\\\"", "\"");
        test("\\\"Hello World!\\\"", "\"Hello World!\"");
        test("\\\"Hello World!\\\\", "\"Hello World!\\");
        test("\\\"\\\"\\\"\\\"", "\"\"\"\"");
        test("The quick brown fox \\\"Jumped\\\" over the lazy dog.",
                "The quick brown fox \"Jumped\" over the lazy dog.");
    }

    /**
     * Tests control characters that can be represented by an escape lateral followed by a char: such as "\n".
     */
    @Test
    public void testSimpleControls(){
        test("\\r", "\r");
        test("\\f", "\f");
        test("\\b", "\b");
        test("\\n", "\n");
        test("\\\\x", "\\x");
        test("Hello \\n World!", "Hello \n World!");
        test("Hello \\t World!", "Hello \t World!");
        test("Hello \\b World!", "Hello \b World!");
        test("Hello World!\\n", "Hello World!\n");
        test("\\n\\r\\f\\b\\t", "\n\r\f\b\t");
        test("\\nHello\\rWorld\\fHello\\bWorld\\tHello", "\nHello\rWorld\fHello\bWorld\tHello");
        test("\\\\n", "\\n");
        test("\\\\t", "\\t");
    }

    /**
     * Tests other control characters, excluding \b, \t, \f, \n, or \r.
     */
    @Test
    public void testControlChars() {
        for(int i = 0; i <= 31; i++){
            char c = (char) i;
            if(c == '\b' || c == '\t' || c == '\f' || c == '\r' || c == '\n')
                continue;
            String unicode = "\\u" + String.format("%04X", i);
            test(unicode, String.valueOf(c));
            test("Hello " + unicode + " World!", "Hello " + c + " World!");
        }

        test("The\\u0000Brown\\u001FFox\\u0002Jumped\\u0004Over\\u000FThe\\u000BLazy\\u0000Dog",
                "The\u0000Brown\u001FFox\u0002Jumped\u0004Over\u000FThe\u000BLazy\u0000Dog");

        test("\\u0000\\b\\u001F\\t\\f\\u001A", "\u0000\b\u001F\t\f\u001A");
    }

    /**
     * Tests everything together.
     */
    @Test
    public void testTogether(){
        String t1 = """
                Entry 5 \\\\ "Contradiction" \n\n.\u0000\r%\u001F.\\n.
                """;
        String a1 = """
                Entry 5 \\\\\\\\ \\"Contradiction\\" \\n\\n.\\u0000\\r%\\u001F.\\\\n.\\n""";
        test(a1, t1);
    }

    private void test(String expected, String toSanitise){
        assertEquals(expected, sanitiseString(toSanitise));
    }

}
