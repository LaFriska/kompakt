import com.friska.kompakt.JSONSerialisable;
import com.friska.kompakt.annotations.DeepSerialise;
import com.friska.kompakt.annotations.Ignored;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This is a simple test-class that tests the {@link Ignored} annotation.
 */
public class IgnoredTest {

    @Test
    public void test(){
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

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }

}
