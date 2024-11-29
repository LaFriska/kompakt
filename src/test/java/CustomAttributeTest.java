import com.friska.kompakt.Attribute;
import com.friska.kompakt.JSONSerialisable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests custom implementations of {@link JSONSerialisable#jsonAttributes()}.
 */
public class CustomAttributeTest {

    @Test
    public void testSimple(){

        record Person(String firstname, String lastname, int age) implements JSONSerialisable{
            @Override
            public List<Attribute> jsonAttributes() {
                ArrayList<Attribute> list = new ArrayList<>();
                list.add(new Attribute("name", firstname + " " + lastname));
                list.add(new Attribute("age", age));
                return list;
            }
        }

        record Trivial() implements JSONSerialisable{
            @Override
            public List<Attribute> jsonAttributes() {
                ArrayList<Attribute> list = new ArrayList<>();
                list.add(new Attribute("hello", null));
                list.add(new Attribute("hello2", "world"));
                return list;
            }
        }

        record Contrived(HashSet<String> set, Integer[] ints) implements JSONSerialisable{
            @Override
            public List<Attribute> jsonAttributes() {
                return new ArrayList<>();
            }
        }

        testClean("""
                {
                  "name": "Peter Sladkovic",
                  "age": 23
                }
                """, new Person("Peter", "Sladkovic", 23));
        testClean("""
                {
                  "name": "John Doe",
                  "age": -20
                }
                """, new Person("John", "Doe", -20));

        testClean("""
                {
                  "hello": null,
                  "hello2": "world"
                }
                """, new Trivial());

        HashSet<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");

        testClean("""
                {
                }
                """, new Contrived(set, null));

    }

    record Person(
            String firstname,
            String lastname,
            int age,
            boolean isAdult,
            Person mother,
            Person father,
            Person grandmother,

            Person grandfather
    ) implements JSONSerialisable {
        @Override
        public List<Attribute> jsonAttributes() {
            ArrayList<Attribute> list = new ArrayList<>();
            list.add(new Attribute("name", this.firstname + " " + this.lastname));
            list.add(new Attribute("age", new Object[]{age, this.isAdult}));
            list.add(new Attribute("family", new Person[]{
                    mother, father, grandmother, grandfather
            }));
            return list;
        }
    }

    @Test
    public void testComplex(){

        Person grandfather = new Person("John", "Doe", 78, true, null, null, null, null);
        Person grandmother = new Person("Jane", "Doe", 75, true, null, null, null, null);
        Person father = new Person("James", "Doe", 50, true, null, null, grandmother, grandfather);
        Person mother = new Person("Mary", "Smith", 48, true, null, null, null, null);
        Person child = new Person("Alice", "Doe", 20, false, mother, father, grandmother, grandfather);

        String expectedJson = """
            {
                "name": "Alice Doe",
                "age": [20, false],
                "family": [
                    {
                        "name": "Mary Smith",
                        "age": [48, true],
                        "family": [null, null, null, null]
                    },
                    {
                        "name": "James Doe",
                        "age": [50, true],
                        "family": [
                            null,
                            null,
                            {
                                "name": "Jane Doe",
                                "age": [75, true],
                                "family": [null, null, null, null]
                            },
                            {
                                "name": "John Doe",
                                "age": [78, true],
                                "family": [null, null, null, null]
                            }
                        ]
                    },
                    {
                        "name": "Jane Doe",
                        "age": [75, true],
                        "family": [null, null, null, null]
                    },
                    {
                        "name": "John Doe",
                        "age": [78, true],
                        "family": [null, null, null, null]
                    }
                ]
            }
        """;

        testClean(expectedJson, child);

    }

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }
}
