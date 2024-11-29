import com.friska.kompakt.JSONSerialisable;
import org.junit.Test;

import java.util.HashSet;

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
        testClean("""
                {
                  "sex": false,
                  "walkingSpeed": 200.0,
                  "name": "Cat",
                  "age": 10,
                  "species": "Feline"
                }
                """, new Mammal("Cat", 10, "Feline", false, 200));

        testClean("""
                {
                  "habitatDepth": -523.0,
                  "swimmingSpeed": 10.0,
                  "name": "Jellyfish",
                  "age": 129,
                  "species": "Single Cell"
                }
                """, new Fish("Jellyfish", 129, "Single Cell", -523, 10));
    }

    /**
     * Tests more than one level of inheritance.
     */
    @Test
    public void testComplex(){
        class Jellyfish extends Fish implements JSONSerialisable{

            Fish[] victims;

            public Jellyfish(String name, int age, Fish... victims) {
                super(name, age, "Jellyfish", -500, 12);
                this.victims = victims;
            }

            @Override
            public boolean deepSerialise() {
                return super.deepSerialise();
            }
        }

        Fish victim1 = new Fish("Goldfish", 2, "Carassius", -10, 1);
        Fish victim2 = new Fish("Tuna", 5, "Thunnus", -100, 15);

        Jellyfish predator = new Jellyfish("Box Jellyfish", 1, victim1, victim2);

        testClean("""
            {
              "victims": [
                {
                  "habitatDepth": -10.0,
                  "swimmingSpeed": 1.0,
                  "name": "Goldfish",
                  "age": 2,
                  "species": "Carassius"
                },
                {
                  "habitatDepth": -100.0,
                  "swimmingSpeed": 15.0,
                  "name": "Tuna",
                  "age": 5,
                  "species": "Thunnus"
                }
              ],
              "habitatDepth": -500.0,
              "swimmingSpeed": 12.0,
              "name": "Box Jellyfish",
              "age": 1,
              "species": "Jellyfish"
            }
            """, predator);

        Jellyfish loneJellyfish = new Jellyfish("Lonely Jelly", 200);

        testClean("""
            {
              "victims": [],
              "habitatDepth": -500.0,
              "swimmingSpeed": 12.0,
              "name": "Lonely Jelly",
              "age": 200,
              "species": "Jellyfish"
            }
            """, loneJellyfish);

    }

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }

    //-------------------------------------CLASSES------------------------------------------

    record Person(String name, int age, boolean isDead) implements JSONSerialisable{
        @Override
        public boolean deepSerialise() {
            return true;
        }
    }

    static class Animal {
        // Attributes
        private String name;
        private int age;
        private String species;

        // Constructor
        public Animal(String name, int age, String species) {
            this.name = name;
            this.age = age;
            this.species = species;
        }
    }

    static class Mammal extends Animal implements JSONSerialisable{

        static int MAMMALS_AVAILABLE = 23;

        boolean sex;

        float walkingSpeed;

        public Mammal(String name, int age, String species, boolean sex, float walkingSpeed) {
            super(name, age, species);
            this.sex = sex;
            this.walkingSpeed = walkingSpeed;
        }

        @Override
        public boolean deepSerialise() {
            return true;
        }
    }

    static class Fish extends Animal implements JSONSerialisable{

        static int FISH_AVAILABLE = 10;
        float habitatDepth;
        float swimmingSpeed;

        public Fish(String name, int age, String species, float habitatDepth, float swimmingSpeed) {
            super(name, age, species);
            this.habitatDepth = habitatDepth;
            this.swimmingSpeed = swimmingSpeed;
        }

        @Override
        public boolean deepSerialise() {
            return true;
        }
    }

}
