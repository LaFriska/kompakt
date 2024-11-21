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

        SimplePerson t1 = new SimplePerson("Peter", 18, 182.2F, 76.3F, true);
        SimplePerson t2 = new SimplePerson("Jake", 0, -30F, 8F, false);
        Book t3 = new Book("The search for modern China", 1990, "Jonathan D. Spence");
        BookShelf t4 = new BookShelf("horror", new Book[]{
                new Book("book1", 1990, "John Doe"),
                new Book("book2", -1000, "Jack Doe"),
                new Book("book3", 0, "Foo Bar"),
                new Book(null, 0, ""),
        });
        BookShelf t5 = new BookShelf(null, new Book[]{null});

        String s1 = """
                {
                  "name": "Peter",
                  "age": 18,
                  "height": 182.2,
                  "weight": 76.3,
                  "isDeceased": true
                }
                """;
        String s2 = """
                {
                  "name": "Jake",
                  "age": 0,
                  "height": -30.0,
                  "weight": 8.0,
                  "isDeceased": false
                }
                """;

        String s3 = """
                {
                  "title": "The search for modern China",
                  "year": 1990,
                  "author": "Jonathan D. Spence"
                }
                """;

        String s4 = """
                {
                  "category": "horror",
                  "books": [
                    {
                      "title": "book1",
                      "year": 1990,
                      "author": "John Doe"
                    },
                    {
                      "title": "book2",
                      "year": -1000,
                      "author": "Jack Doe"
                    },
                    {
                      "title": "book3",
                      "year": 0,
                      "author": "Foo Bar"
                    },
                    {
                      "title": null,
                      "year": 0,
                      "author": ""
                    }
                  ]
                }
                """;

        String s5 = """
                {
                  "category": null,
                  "books": [
                    null
                  ]
                }
                """;


        JSONSerialisable[] objects = new JSONSerialisable[]{t1, t2, t3, t4, t5};
        String[] expectedStrings = new String[]{s1, s2, s3, s4, s5};

        for (int i = 0; i < expectedStrings.length; i++)
            testClean(expectedStrings[i], objects[i]);

    }

    /**
     * Tests complex scenarios with objects nested in one another.
     */
    @Test
    public void testComplicated() {

        SimplePerson parent = new SimplePerson("Alice", 45, 165.0F, 65.0F, false);
        SimplePerson child1 = new SimplePerson("Bob", 20, 175.5F, 70.2F, false);
        SimplePerson child2 = new SimplePerson("Charlie", 18, 180.3F, 75.5F, false);

        Family family = new Family(
                parent,
                new SimplePerson[]{child1, child2},
                "Smith"
        );

        BookShelf fantasyShelf = new BookShelf("fantasy", new Book[]{
                new Book("The Hobbit", 1937, "J.R.R. Tolkien"),
                new Book("Harry Potter", 1997, "J.K. Rowling")
        });

        BookShelf scienceShelf = new BookShelf("science", new Book[]{
                new Book("A Brief History of Time", 1988, "Stephen Hawking"),
                new Book("The Selfish Gene", 1976, "Richard Dawkins")
        });

        Library library = new Library(new BookShelf[]{fantasyShelf, scienceShelf});

        String familyExpected = """
            {
              "parent": {
                "name": "Alice",
                "age": 45,
                "height": 165.0,
                "weight": 65.0,
                "isDeceased": false
              },
              "children": [
                {
                  "name": "Bob",
                  "age": 20,
                  "height": 175.5,
                  "weight": 70.2,
                  "isDeceased": false
                },
                {
                  "name": "Charlie",
                  "age": 18,
                  "height": 180.3,
                  "weight": 75.5,
                  "isDeceased": false
                }
              ],
              "familyName": "Smith"
            }
            """;

        String libraryExpected = """
            {
              "shelves": [
                {
                  "category": "fantasy",
                  "books": [
                    {
                      "title": "The Hobbit",
                      "year": 1937,
                      "author": "J.R.R. Tolkien"
                    },
                    {
                      "title": "Harry Potter",
                      "year": 1997,
                      "author": "J.K. Rowling"
                    }
                  ]
                },
                {
                  "category": "science",
                  "books": [
                    {
                      "title": "A Brief History of Time",
                      "year": 1988,
                      "author": "Stephen Hawking"
                    },
                    {
                      "title": "The Selfish Gene",
                      "year": 1976,
                      "author": "Richard Dawkins"
                    }
                  ]
                }
              ]
            }
            """;

        testClean(libraryExpected, library);
        testClean(familyExpected, family);
    }


    public <T extends JSONSerialisable> void testClean(String expected, T obj){
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

    record Family(SimplePerson parent, SimplePerson[] children, String familyName) implements JSONSerialisable {}

    record Library(BookShelf[] shelves) implements JSONSerialisable {
        static final String LIBRARY_NAME = "Genesis";
    }

    record Book(String title, int year, String author) implements JSONSerialisable{}

    record BookShelf(String category, Book[] books) implements JSONSerialisable{}

}
