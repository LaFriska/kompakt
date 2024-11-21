import com.friska.JSONSerialisable;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class JSONSerialisableTest {


    /**
     * Tests whether the JSON strings are trivial when it should be trivial. For example, {@link JSONSerialisable} should
     * ignore static fields.
     */
    @Test
    public void testTrivial(){

        class EmptyClassMethod implements JSONSerialisable {
            public void funny(){
                System.out.println("hi");
            }
        }
        class EmptyClassStatic implements JSONSerialisable {

            public static final float PI = 3.14F;

            public void funny(){
                System.out.println("hi");
            }
        }

        class EmptyClassContrived implements JSONSerialisable{
            public static final float PI = 3.14F;

            public static String TEXT = "Hello World";

            public static HashSet<HashSet<EmptyClassContrived>> set = new HashSet<>();
        }

        class NonEmptyContrived implements JSONSerialisable{

            EmptyClass obj1 = new EmptyClass();

            EmptyClass obj2 = new EmptyClass();

            EmptyClass obj3 = new EmptyClass();

            static EmptyClass obj4 = new EmptyClass();

        }

        testClean("{}", new EmptyClass());
        testClean("{}", new EmptyClassMethod());
        testClean("{}", new EmptyClassContrived());
        testClean("{\"obj1\":{},\"obj2\":{},\"obj3\":{}}", new NonEmptyContrived());

    }

    /**
     * Tests simple but non-trivial classes.
     */
    @Test
    public void testSimple(){

        SimplePerson p1 = new SimplePerson("Peter", 18, 182.2F, 76.3F, true);

        String s1 = """
                {
                  "name": "Peter",
                  "age": 18,
                  "height": 182.2,
                  "weight": 76.3,
                  "isDeceased": true
                }
                """;

        testClean(s1, p1);

    }

    public<T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }

    //----------------------------------------------CLASSES------------------------------------------------------------

    static class EmptyClass implements JSONSerialisable {}

    static class SimplePerson implements JSONSerialisable{

        public static final float AVG_WEIGHT = 82.5F;

        private String name;

        public final int age;

        public final float height;

        public final float weight;

        protected boolean isDeceased;

        SimplePerson(String name, int age, float height, float weight, boolean isDeceased){
            this.name = name;
            this.age = age;
            this.height = height;
            this.weight = weight;
            this.isDeceased = isDeceased;
        }

    }

}
