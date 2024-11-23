import com.friska.JSONSerialisable;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * This class tests JSON serialisation for when {@link JSONSerialisable#deepSerialise()} is enabled. (Overidden to
 * return true.)
 */
public class DeepSerialiseTest {

    /**
     * Tests whether trivial JSON serialisation still acts appropriately when deepSerialise is enabled.
     */
    @Test
    public void testTrivial(){

        class EmptyClassMethod implements JSONSerialisable {
            public void funny(){
                System.out.println("hi");
            }

            @Override
            public boolean deepSerialise() {
                return true;
            }
        }
        class EmptyClassStatic implements JSONSerialisable {

            public static final float PI = 3.14F;

            public void funny(){
                System.out.println("hi");
            }

            @Override
            public boolean deepSerialise() {
                return true;
            }
        }

        class EmptyClassContrived implements JSONSerialisable{
            public static final float PI = 3.14F;

            public static String TEXT = "Hello World";

            public static HashSet<HashSet<EmptyClassContrived>> set = new HashSet<>();

            @Override
            public boolean deepSerialise() {
                return true;
            }
        }

        class NonEmptyContrived implements JSONSerialisable{

            SerialiseTest.EmptyClass obj1 = new SerialiseTest.EmptyClass();

            SerialiseTest.EmptyClass obj2 = new SerialiseTest.EmptyClass();

            SerialiseTest.EmptyClass obj3 = new SerialiseTest.EmptyClass();

            static SerialiseTest.EmptyClass obj4 = new SerialiseTest.EmptyClass();

            @Override
            public boolean deepSerialise() {
                return true;
            }

        }

        testClean("{}", new SerialiseTest.EmptyClass());
        testClean("{}", new EmptyClassStatic());
        testClean("{}", new EmptyClassMethod());
        testClean("{}", new EmptyClassContrived());
        testClean("{\"obj1\":{},\"obj2\":{},\"obj3\":{}}", new NonEmptyContrived());

        testClean("""
                {
                  "name": "Peter",
                  "age": 23,
                  "isDead": false
                }
                """, new Person("Peter", 23, false));

        testClean("""
                {
                  "name": null,
                  "age": -3,
                  "isDead": true
                }
                """, new Person(null, -3, true));

    }

    /**
     * Tests simple inheritance for children with a single super-class.
     */
    @Test
    public void testSimple(){

    }

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }

    //-------------------------------------CLASSES------------------------------------------

    enum Law{
        ADDITION,
        CONCAT,
        ARBITRARY,
        MULTIPLICATION,
        EXPONENTIATION,

        CYCLIC
    }

    record Person(String name, int age, boolean isDead) implements JSONSerialisable{
        @Override
        public boolean deepSerialise() {
            return true;
        }
    }

    static class AlgebraicSet{
        Object[] elements;

        AlgebraicSet(Object... objs){
            this.elements = objs;
        }

    }

    static class FiniteGroup extends AlgebraicSet implements JSONSerialisable{

        public final Object additiveID;

        Law additiveComposition;

        FiniteGroup(Object[] elements, Object additiveID, Law additiveComposition){
            super(elements);
            this.additiveID = additiveID;
            this.additiveComposition = additiveComposition;
        }

        @Override
        public boolean deepSerialise() {
            return true;
        }
    }

}
