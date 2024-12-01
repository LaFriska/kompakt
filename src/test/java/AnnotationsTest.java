import com.friska.kompakt.JSONSerialisable;
import com.friska.kompakt.annotations.DeepSerialise;
import com.friska.kompakt.annotations.Ignored;
import com.friska.kompakt.annotations.SerialiseAsString;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * This is a simple test-class that tests various annotation. Since the functionality of these annotations depends
 * on methods in {@link JSONSerialisable}, this class does not extensively tests the functionality, but rather, ensures
 * that the annotations work.
 */
public class AnnotationsTest {

    /**
     * Tests {@link Ignored}.
     */
    @Test
    public void testIgnore(){
        Person1 p = new Person1("Peter", 32, 180);
        testClean("""
                {
                  "height": 180.0
                }
                """, p);

        Child c = new Child("Peter", 32, 180);
        testClean("""
                {
                  "f1": 3,
                  "height": 180.0
                }
                """, c);

        testClean("""
                {
                  "name": "Peter",
                  "age": 32
                }
                """, new Person2("Peter", 32, 180));
    }


    static class Person1 implements JSONSerialisable{

        @Ignored
        private String name;

        @Ignored
        public final int age;

        public final float height;


        Person1(String name, int age, float height){
            this.name = name;
            this.age = age;
            this.height = height;
        }

    }

    record Person2(String name, int age, @Ignored float height) implements JSONSerialisable{

    }

    @DeepSerialise
    static class Child extends Person1 implements JSONSerialisable{

        private int f1 = 3;

        Child(String name, int age, float height) {
            super(name, age, height);
        }
    }

    /**
     * Tests {@link SerialiseAsString}.
     */
    @Test
    public void testSerialiseAsString(){

        class Stuff implements JSONSerialisable{

            @SerialiseAsString
            ArrayList<Name> f1 = new ArrayList<>();

            HashSet<Object> f2 = new HashSet<>();

            @SerialiseAsString
            boolean f3 = true;

            @SerialiseAsString
            int f4 = 35;

            Name[] f5 = new Name[]{
                    new Name("John", "Doe"),
                    new Name("Foo", "Bar")
            };

            @SerialiseAsString
            Object f6 = null;
            Object f7 = null;

            Stuff(){
                f1.add(new Name("John", "Doe"));
                f1.add(new Name("Foo", "Bar"));
                f2.add("Hello World");
            }
        }

        testClean("""
                {
                  "f1": "[John Doe, Foo Bar]",
                  "f2": ["Hello World"],
                  "f3": "true",
                  "f4": "35",
                  "f5": [
                    "John Doe",
                    "Foo Bar"
                  ],
                  "f6": null,
                  "f7": null
                }
                """, new Stuff());
        testClean(
                """
                        {
                          "f1": "3.14",
                          "f2": "false"
                        }
                        """,
                new RecordStuff(3.14F, false)
        );
    }

    record RecordStuff(@SerialiseAsString float f1,
                       @SerialiseAsString boolean f2
    ) implements JSONSerialisable{}

    record Name(String fName, String lName){
        @Override
        public String toString() {
            return fName + " " + lName;
        }
    }

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }

}
