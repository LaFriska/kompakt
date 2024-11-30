import com.friska.kompakt.JSONObject;
import com.friska.kompakt.JSONParser;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.friska.kompakt.JSONParser.*;
import static com.friska.kompakt.NumberType.*;

/**
 * This class tests that serialisation composed with deserialisation and vice-versa is the identity function.
 * We decided that testing trivial cases is redundant, as these tests cases are already covered by the other test
 * classes, hence, this class will immediately resort to complex JSONs. For simplicity, the type of each JSON string will
 * be object.
 */
public class IdentityTest {

    @Test
    public void testMedium(){

        String s1 = """
                {
                  "Hello": {
                    "array": [
                      1,2,3,4,5,null
                    ]
                  }
                }
                """;

        String s2 = """
                {
                   "Hello": {
                     "array": [
                       1,2,3,4,5,null, {
                         "Hello": "\\n\\n\\n\\t\\t\\t"
                       },
                       1.23
                     ],
                     "obj": {
                 
                     }
                   }
                 }
                """;
        String s3 = """
                {
                  "array": [
                   2,"hi"
                  ]
                }
                """;
        String s4 = """
                {
                  "array": [
                   2,"hi",[]
                  ]
                }
                """;

        testBigDecimal(s1, s2, s3, s4);
    }

    private void testBigDecimal(String... jsons){
        for (String json : jsons) {
            JSONObject exp1 = parseAsObject(json, BIGDECIMAL);
            assertEquals(parseAsObject(exp1.serialise(), BIGDECIMAL), exp1);
        }
    }

}
