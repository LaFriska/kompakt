import com.friska.JSONDeserialiser;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.friska.JSONDeserialiser.parseString;

/**
 * This class extensively tests {@link JSONDeserialiser#parseString(String)}.
 */
public class StringParserTest {

    /**
     * Tests trivial invalid JSON strings.
     */
    @Test
    public void testInvalidTrivial(){
        testInvalid("");
        testInvalid("\"");
        testInvalid("\"\"\"");
        testInvalid("\"Hello\"World\"");
        testInvalid("\"\"s");
        testInvalid("\"Hello World\"s");
        testInvalid("s\"Hello World\"");
        testInvalid("\"Hello World\"s\"");
        testInvalid("null ");
    }

    /**
     * Tests valid JSON strings, trivial.
     */
    @Test
    public void testValidTrivial(){
        test("", "\"\"");
        test(null, "null");
        test("a", "\"a\"");
        test("Hello World!", "\"Hello World!\"");
    }

    /**
     * Tests basic escape laterals.
     */
    @Test
    public void testEscape(){
        test("\n", "\"\\n\"");
        test("\r", "\"\\r\"");
        test("\t", "\"\\t\"");
        test("\f", "\"\\f\"");
        test("\b", "\"\\b\"");
        test("/", "\"\\/\"");
        test("/", "\"/\"");
        test("//", "\"\\//\"");
        test("\\", "\"\\\\\"");
        test("\"", "\"\\\"\"");
        test("\\ \\", "\"\\\\ \\\\\"");
        test("\\ Hello \" World \\", "\"\\\\ Hello \\\" World \\\\\"");
        test("\\ Hello \" World \\\"", "\"\\\\ Hello \\\" World \\\\\\\"\"");
        test("\"Hungarian Rhapsody\"\n\n\tThe Hungarian Rhapsody is a beautiful composition. \\\\",
                "\"\\\"Hungarian Rhapsody\\\"\\n\\n\\tThe Hungarian Rhapsody is a beautiful composition. \\\\\\\\\"");
        test("\n\t\r\\\\\f\f\n\r\\\\\b\n\b\"\"\"\\\\\\\\\\",
                "\"\\n\\t\\r\\\\\\\\\\f\\f\\n\\r\\\\\\\\\\b\\n\\b\\\"\\\"\\\"\\\\\\\\\\\\\\\\\\\\\"");
        test("The\"\"\\ \\Quick\n Br\rown\r \nF\\\\\\\\ox Ju\f\f\fm\rp\be\bd\b O\nver \\\\The L\"\"\"azy Dog!",
                "\"The\\\"\\\"\\\\ \\\\Quick\\n Br\\rown\\r \\nF\\\\\\\\\\\\\\\\ox Ju\\f\\f\\fm\\rp\\be\\b" +
                        "d\\b O\\nver \\\\\\\\The L\\\"\\\"\\\"azy Dog!\"");
    }

    /**
     * Tests most invalid cases.
     */
    @Test
    public void testInvalid(){
        testInvalid("\"\\a\"");
        testInvalid("\"\\v\"");
        testInvalid("\"\\xyz\"");
        testInvalid("\"\\u\"");
        testInvalid("\"\\b\\u\\f\"");
        testInvalid("\"\\ \"\"");
        testInvalid("\"\b\b\\\"");
        testInvalid("\"\u0000\"");
        testInvalid("\"\u0002\"");
        testInvalid("\"\u0005\"");
        testInvalid("\"\u001F\"");
        testInvalid("\"\u0001\"");
        testInvalid("\"\u0011\"");
        testInvalid("\"\\\\\"\"");
        testInvalid("\"Hello World \\\\\" Quick Brown Fox\"");
        testInvalid("\"Hello World Quick Brown Fox");
        testInvalid("\"Hello World Quick Br\\own Fox\"");

        //Tests invalid unicodes
        testInvalid("\"\\u000\"");
        testInvalid("\"\\u00h0\"");
        testInvalid("\"\\0001\"");
        testInvalid("\"\\u00\"");
        testInvalid("\"\\u\"");
        testInvalid("\"\\u0-00\"");
        testInvalid("\"\\0000u\"");
        testInvalid("\"\\u[000\"");
        testInvalid("\"\\u0[00\"");
        testInvalid("\"\\u00[0\"");
        testInvalid("\"\\u000[\"");
        testInvalid("\"\\u0000");
        testInvalid("\\u0000\"");
    }

    /**
     * Tests control character unicode strings to ensure every control character may be deserialised.
     */
    @Test
    public void testUnicodeTrivial(){
        test("\u0000", "\"\\u0000\"");
        test("\u0001", "\"\\u0001\"");
        test("\u0002", "\"\\u0002\"");
        test("\u0003", "\"\\u0003\"");
        test("\u0004", "\"\\u0004\"");
        test("\u0005", "\"\\u0005\"");
        test("\u0006", "\"\\u0006\"");
        test("\u0007", "\"\\u0007\"");
        test("\u0008", "\"\\u0008\"");
        test("\t", "\"\\u0009\"");
        test("\n", "\"\\u000A\"");
        test("\u000B", "\"\\u000B\"");
        test("\u000C", "\"\\u000C\"");
        test("\r", "\"\\u000D\"");
        test("\u000E", "\"\\u000E\"");
        test("\u000F", "\"\\u000F\"");
        test("\u0010", "\"\\u0010\"");
        test("\u0011", "\"\\u0011\"");
        test("\u0012", "\"\\u0012\"");
        test("\u0013", "\"\\u0013\"");
        test("\u0014", "\"\\u0014\"");
        test("\u0015", "\"\\u0015\"");
        test("\u0016", "\"\\u0016\"");
        test("\u0017", "\"\\u0017\"");
        test("\u0018", "\"\\u0018\"");
        test("\u0019", "\"\\u0019\"");
        test("\u001A", "\"\\u001A\"");
        test("\u001B", "\"\\u001B\"");
        test("\u001C", "\"\\u001C\"");
        test("\u001D", "\"\\u001D\"");
        test("\u001E", "\"\\u001E\"");
        test("\u001F", "\"\\u001F\"");
    }

    /**
     * Tests unicode in medium-sized strings.
     */
    @Test
    public void testUnicode(){
        test("\u0017\u001F\u0007\u001F", "\"\\u0017\\u001F\\u0007\\u001F\"");
        test("Hello\u001FWorld\nHi\u0000", "\"Hello\\u001FWorld\\nHi\\u0000\"");
        test("Th\u001Fe Qu\u0017ick\u0011 \u0008\u0008\u0008Br\u001Down\u0000" +
                " Fo\u000Fx Jump\u0010ed Ov\u0010er \u0010The La\u0010zy \u0010Dog.",
                "\"Th\\u001Fe Qu\\u0017ick\\u0011 \\u0008\\u0008\\u0008Br\\u001Down\\u0000 " +
                        "Fo\\u000Fx Jump\\u0010ed Ov\\u0010er \\u0010The La\\u0010zy \\u0010Dog.\"");

        testSelfUnicode("Hello World!");
        testSelfUnicode("!@#$%^&*()_+-+=0987654321");
        testSelfUnicode("qwertyuiop[]]\\';lkjhgfdsazxcvbnm,.//.,mn");
        testSelfUnicode("\"The\\\"\\\"\\\\ \\\\Quick\\n Br\\rown\\r \\nF\\\\\\\\\\\\\\\\ox Ju\\f\\f\\fm\\rp\\be\\b" +
                "d\\b O\\nver \\\\\\\\The L\\\"\\\"\\\"azy Dog!\"");
    }

    /**
     * Contrived test case to make sure everything works together.
     */
    @Test
    public void testContrived(){
        test(
                "k\n\n\n\n\n\r\r\"\r\r\raj\u0000s\\u0/0/0/0bk\u0001j\n\n\n\n\\ae\\\\\\\\\\b\"\"\\\"jkbakj" +
                        "db\"\"\"mbvks\\jj^*@(\\\\\\\\O\"\"\\u0001W\u0000I\\\"\"f\f\f\f\f\f\n\f\b\b\bh@(B@OIbo" +
                        "bOF\\O*O@BFOBOCIO@BLIF",
                "\"k\\n\\n\\n\\n\\n\\r\\r\\\"\\r\\r\\raj\\u0000s\\\\u0\\/0\\/0/0bk\\u0001j\\n\\n\\n\\n\\\\a" +
                        "e\\\\\\\\\\\\\\\\\\\\b\\\"\\\"\\\\\\\"jkbakjdb\\\"\\\"\\\"mbvks\\\\jj^" +
                        "*@(\\\\\\\\\\\\\\\\O\\\"\\\"\\\\u0001W\\u0000I\\\\\\\"\\\"f\\f\\f\\f\\f\\f\\n\\f\\b\\b\\bh@" +
                        "(B@OIbobOF\\\\O*O@BFOBOCIO@BLIF\""
        );
    }

    private void test(String actual, String toParse){
        assertEquals(actual, parseString(toParse));
    }
    private void testInvalid(String jsonString){
        assertThrows(IllegalArgumentException.class, () -> parseString(jsonString));
    }

    private void testSelfUnicode(String str){
        assertEquals(str, parseString(convertToUnicodeString(str)));
    }
    private static String convertToUnicodeString(@NotNull String str){
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for(int i = 0; i < str.length(); i++){
            sb.append("\\" + "u").append(String.format("%04X", (int) str.charAt(i)));
        }
        sb.append("\"");
        return sb.toString();
    }

}
