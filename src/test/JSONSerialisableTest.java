import com.friska.JSONSerialisable;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class JSONSerialisableTest {

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

    public<T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(expected, Utils.strip(obj.serialise()));
    }

    static class EmptyClass implements JSONSerialisable {}

}
