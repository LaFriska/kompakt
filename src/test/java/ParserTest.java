import com.friska.Attribute;
import com.friska.JSONObject;
import com.friska.JSONParser;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static com.friska.JSONParser.*;
import static com.friska.NumberType.*;

/**
 * This class tests the ultimate deserialisation process, i.e. {@link JSONParser#parse(String)}
 */
public class ParserTest {

    public static final Class<IllegalArgumentException> ERROR_CLASS = IllegalArgumentException.class;

    /**
     * Tests trivial invalid JSON strings.
     */
    @Test
    public void testInvalidTrivial(){
        testInvalid("");
        testInvalid("   ");
        testInvalid("{");
        testInvalid("}");
        testInvalid("[");
        testInvalid("]");
        testInvalid("\"Hello World");
        testInvalid("Hello World\"");
        testInvalid("Hello World");
        testInvalid(".523");
        testInvalid("0 .523");
        testInvalid("249 2380");
        testInvalid("23523532.925232.");
        testInvalid("-23523.523e2.3");
        testInvalid("e2.3");
        testInvalid("\"Hello \\world\"");
    }

    /**
     * Tests simple non-recursive data types.
     */
    @Test
    public void testValidTrivial(){

        //STRINGS

        assertEquals("", parseAsString("\"\""));
        assertEquals("Hello World!", parseAsString("\"Hello World!\""));
        assertEquals("Hello World!", parseAsString("     \"Hello World!\"\n\n\n\n"));
        assertEquals("\"Hello World!\"", parseAsString("\"\\\"Hello World!\\\"\""));
        assertEquals("\"Hello World!\"", parseAsString("\n\n\n\n\n\n\n\"\\\"Hello World!\\\"\"    "));

        //Copied over from StringParserTest
        assertEquals("k\n\n\n\n\n\r\r\"\r\r\raj\u0000s\\u0/0/0/0bk\u0001j\n\n\n\n\\ae\\\\\\\\\\b\"\"\\\"jkbakj" +
                "db\"\"\"mbvks\\jj^*@(\\\\\\\\O\"\"\\u0001W\u0000I\\\"\"f\f\f\f\f\f\n\f\b\b\bh@(B@OIbo" +
                "bOF\\O*O@BFOBOCIO@BLIF",
                parseAsString("\"k\\n\\n\\n\\n\\n\\r\\r\\\"\\r\\r\\raj\\u0000s\\\\u0\\/0\\/0/0bk\\u0001j\\n\\n\\n\\n\\\\a" +
                "e\\\\\\\\\\\\\\\\\\\\b\\\"\\\"\\\\\\\"jkbakjdb\\\"\\\"\\\"mbvks\\\\jj^" +
                "*@(\\\\\\\\\\\\\\\\O\\\"\\\"\\\\u0001W\\u0000I\\\\\\\"\\\"f\\f\\f\\f\\f\\f\\n\\f\\b\\b\\bh@" +
                "(B@OIbobOF\\\\O*O@BFOBOCIO@BLIF\"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n \t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n \t\t\t \t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"));

        //NUMBERS

        assertEquals(23, parseAsNumber("23", INT));
        assertEquals(23, parseAsNumber("    \n      23\n\n", INT));
        assertEquals(Integer.MAX_VALUE, parseAsNumber("2147483647", INT));
        assertEquals((float) 23.5, parseAsNumber("23.5"));
        assertEquals(new BigDecimal("23.2957222e-23"), parseAsNumber("23.2957222e-23", BIGDECIMAL));
        assertEquals(new BigDecimal("23.2957222E-23"), parseAsNumber("23.2957222E-23", BIGDECIMAL));
        assertEquals(new BigDecimal("23.2957222E-23"), parseAsNumber("\n \n23.2957222E-23   \n ", BIGDECIMAL));
        assertEquals(1234567890.1, parseAsNumber("1234567890.1 ", DOUBLE));

        //BOOLEANS

        assertEquals(true, parseAsBool("true"));
        assertEquals(false, parseAsBool("false"));
        assertEquals(true, parseAsBool("   true \t     \n"));
        assertEquals(false, parseAsBool("false\n  \n"));

        //NULL
        assertNull(parse("null"));
        assertNull(parse("\n\nnull  "));
    }

    /**
     * Tests simple JSONs with type object.
     */
    @Test
    public void testObjectValidSimple(){

        String s0 = "{    }";

        JSONObject o0 = new JSONObject();

        String s1 = """
                {
                  "name": "Peter",
                  "age": 32,
                  "gpa": 6.45,
                  "isDead": false,
                  "education": null
                }
                """;
        JSONObject o1 = new JSONObject();
        o1.addAttributes(
                new Attribute("name", "Peter"),
                new Attribute("age", 32.0F),
                new Attribute("gpa", 6.45F),
                new Attribute("isDead", false),
                new Attribute("education", null)
        );

        String s2 = """
                                       {
                  "\\"field 1\\""      :    0,
                  "field2"
                         \s
                         \s
                         \s
                          :              false              ,
                  "field   3" : "hi"
                                        \s
                                        \s
                                        \s
                                        \s
                                        \s
                                        \s
                }
                """;
        JSONObject o2 = new JSONObject();
        o2.addAttributes(
                new Attribute("\"field 1\"", 0F),
                new Attribute("field2", false),
                new Attribute("field   3", "hi")
        );

        String s3 = """
                {
                  "nest1": {
                    "nest2": {
                      "field": null
                    }
                  }
                }
                """;
        JSONObject nest2 = new JSONObject();
        nest2.addAttribute("field", null);
        JSONObject nest1 = new JSONObject();
        nest1.addAttribute("nest2", nest2);
        JSONObject o3 = new JSONObject();
        o3.addAttribute("nest1", nest1);

        String s4 = """
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                   \s
                  }
                }
                """;

        JSONObject o4 = new JSONObject();
        o4.addAttribute("\\", nest1);
        o4.addAttribute("\"", nest2);
        o4.addAttribute("empty", new JSONObject());

        String[] toParse = new String[]{s0, s1, s2, s3, s4};
        JSONObject[] expected = new JSONObject[]{o0, o1, o2, o3, o4};

        for (int i = 0; i < toParse.length; i++) {
            assertEquals(expected[i], parse(toParse[i]));
        }
    }

    /**
     * Tests simple invalid objects.
     */
    @Test
    public void testObjectInvalidSimple(){
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }}
                """);
        testInvalid("""
                
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {{
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  }
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  },
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                        "hi"        
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null"
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "fie\\ld": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": 
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field" null
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": null
                    }
                  },
                  "\\"": :{
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
        testInvalid("""
                {
                  "\\\\": {
                    "nest2": {
                      "field": 23.
                    }
                  },
                  "\\"": {
                    "field": null
                  },
                  "empty": {
                                
                  }
                }
                """);
    }

    private void testInvalid(@NotNull String json){
        assertThrows(ERROR_CLASS, () -> parse(json));
    }
}
