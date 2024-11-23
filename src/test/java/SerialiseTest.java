import com.friska.JSONSerialisable;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.junit.Assert.*;

/**
 * This class extensively tests {@link JSONSerialisable#serialise()}.
 */
public class SerialiseTest {


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
        testClean("{}", new EmptyClassStatic());
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


    /**
     * Tests contrived and complicated object nesting.
     */
    @Test
    public void testContrived(){
        ContrivedTree<String> t1 = new ContrivedTree<>(null, "Cat", null);
        ContrivedTree<String> t2 = new ContrivedTree<>(null, "Dog", null);
        ContrivedTree<Boolean> t3 = new ContrivedTree<>(t1, true, t2);
        ContrivedTree<Boolean> t4 = new ContrivedTree<>(t1, true,
                new ContrivedTree<>(null, "Dog",
                        new ContrivedTree<>(null, 0.5,
                                new ContrivedTree<>(null, null, null))));

        ContrivedTree<ContrivedTree<ContrivedTree<Integer>>> t5 = new ContrivedTree<>(
                new ContrivedTree<>(
                        new ContrivedTree<>(null, 1, null),
                        new ContrivedTree<>(null, 2, null),
                        new ContrivedTree<>(null, 3, null)
                ),
                new ContrivedTree<>(
                        new ContrivedTree<>(null, 4, null),
                        new ContrivedTree<>(null, 5, null),
                        new ContrivedTree<>(null, 6, null)
                ),
                new ContrivedTree<>(
                        new ContrivedTree<>(null, 7, null),
                        new ContrivedTree<>(null, 8, null),
                        new ContrivedTree<>(null, 9, null)
                )
        );

        ContrivedTree<ContrivedTree<Object[]>> t6 = new ContrivedTree<>(
                new ContrivedTree<>(
                        new ContrivedTree<>(null, null, null),
                        new ContrivedTree<>(null, new Object[]{"Cat", "Dog", 1, 2, 3}, null),
                        new ContrivedTree<>(null, null, null)
                ),
                new ContrivedTree<>(
                        new ContrivedTree<>(
                                null,
                                new Object[]{'A', "B", true, false, new Object[]{"Cat", "Dog",
                                        new ContrivedTree<>(null, null, null)}},
                                null
                        ),
                        null,
                        null
                ),
                null
        );


        String s1 = """
                {
                  "left": null,
                  "node": "Cat",
                  "right": null
                }
                """;
        String s2 = """
                {
                  "left": null,
                  "node": "Dog",
                  "right": null
                }
                """;
        String s3 = """
                {
                  "left": {
                    "left": null,
                    "node": "Cat",
                    "right": null
                  },
                  "node": true,
                  "right": {
                    "left": null,
                    "node": "Dog",
                    "right": null
                  }
                }
                """;

        String s4 = """
                {
                  "left": {
                    "left": null,
                    "node": "Cat",
                    "right": null
                  },
                  "node": true,
                  "right": {
                    "left": null,
                    "node": "Dog",
                    "right": {
                      "left": null,
                      "node": 0.5,
                      "right": {
                        "left": null,
                        "node": null,
                        "right": null
                      }
                    }
                  }
                }
                """;

        String s5 = """
                {
                  "left": {
                    "left": {
                      "left": null,
                      "node": 1,
                      "right": null
                    },
                    "node": {
                      "left": null,
                      "node": 2,
                      "right": null
                    },
                    "right": {
                      "left": null,
                      "node": 3,
                      "right": null
                    }
                  },
                  "node": {
                    "left": {
                      "left": null,
                      "node": 4,
                      "right": null
                    },
                    "node": {
                      "left": null,
                      "node": 5,
                      "right": null
                    },
                    "right": {
                      "left": null,
                      "node": 6,
                      "right": null
                    }
                  },
                  "right": {
                    "left": {
                      "left": null,
                      "node": 7,
                      "right": null
                    },
                    "node": {
                      "left": null,
                      "node": 8,
                      "right": null
                    },
                    "right": {
                      "left": null,
                      "node": 9,
                      "right": null
                    }
                  }
                }
                """;

        String s6 = """
                {
                  "left": {
                    "left": {
                      "left": null,
                      "node": null,
                      "right": null
                    },
                    "node": {
                      "left": null,
                      "node": ["Cat", "Dog", 1, 2, 3],
                      "right": null
                    },
                    "right": {
                      "left": null,
                      "node": null,
                      "right": null
                    }
                  },
                  "node": {
                    "left": {
                      "left": null,
                      "node": ["A", "B", true, false, ["Cat", "Dog", {
                        "left": null,
                        "node": null,
                        "right": null
                      }]],
                      "right": null
                    },
                    "node": null,
                    "right": null
                  },
                  "right": null
                }
                """;

        JSONSerialisable[] objects = new JSONSerialisable[]{t1, t2, t3, t4, t5, t6};
        String[] expectedStrings = new String[]{s1, s2, s3, s4, s5, s6};

        for (int i = 0; i < expectedStrings.length; i++)
            testClean(expectedStrings[i], objects[i]);
    }


    /**
     * As specified in the documentation for {@link JSONSerialisable#serialise()}, the serialise method only
     * scans non-inherited fields from the object. This test method ensures that the fields detected by the
     * interface is consistent.
     */
    @Test
    public void testInheritance(){

        class Parent{
            private int pf1;

            public int pf2;

            String pf3;

            Parent(int a, int b, String c){
                pf1 = a;
                pf2 = b;
                pf3 = c;
            }
        }

        class Child extends Parent implements JSONSerialisable{

            private int cf1;

            public String cf2;

            Child(int a, int b, String c, int d, String e) {
                super(a, b, c);
                cf1 = d;
                cf2 = e;
            }
        }

        class GrandChild extends Child{

            private int gf1;

            public String gf2;


            GrandChild(int a, int b, String c, int d, String e, int f, String g) {
                super(a, b, c, d, e);
                this.gf1 = f;
                this.gf2 = g;
            }
        }


        Child test1 = new Child(1, 2, null, 3, "Cat");
        Child test2 = new Child(1, 2, "Cat", Integer.MIN_VALUE, null);
        GrandChild test3 = new GrandChild(1, 2, "Cat", Integer.MIN_VALUE, null, 25, "Hello World");

        testClean("""
                {
                  "cf1": 3,
                  "cf2": "Cat"
                }
                """, test1);

        testClean("""
                {
                  "cf1": -2147483648,
                  "cf2": null
                }
                """, test2);
        testClean("""
                {
                  "gf1": 25,
                  "gf2": "Hello World"
                }
                """, test3);

    }

    @Test
    public void testIgnore(){
        class PersonIgnore implements JSONSerialisable{

            private String name;

            public final int age;

            public final float height;

            public final float weight;

            protected boolean isDeceased;

            PersonIgnore(String name, int age, float height, float weight, boolean isDeceased){
                this.name = name;
                this.age = age;
                this.height = height;
                this.weight = weight;
                this.isDeceased = isDeceased;
            }

            @Override
            public String[] ignoredFields() {
                return new String[]{"height", "isDeceased", "weight"};
            }
        }

        class IgnoreContrived implements JSONSerialisable{

            int f1;

            String f2;

            boolean f3;

            HashSet<HashSet<Map<String, Set<Stack<Book>>>>> f4;

            IgnoreContrived f5;

            public IgnoreContrived(int f1, String f2, boolean f3, HashSet<HashSet<Map<String, Set<Stack<Book>>>>> f4,
                                   IgnoreContrived f5){
                this.f1 = f1;
                this.f2 = f2;
                this.f3 = f3;
                this.f4 = f4;
                this.f5 = f5;
            }

            @Override
            public String[] ignoredFields() {
                return new String[]{"f2", "f3", "f4", "f5", "something else"};
            }
        }

        class IgnoreTrivial implements JSONSerialisable{

            String[] strings = new String[]{"a", "b", "cde", "hello"};

            int[] ints = new int[]{1,2,3,4,5};

            boolean[] bools = null;

            @Override
            public String[] ignoredFields() {
                return new String[]{"strings", "bools", "ints"};
            }
        }

        PersonIgnore t1 = new PersonIgnore("Peter", 23, 172F, 80, false);
        PersonIgnore t2 = new PersonIgnore("John Doe", -1, -1, 23, true);
        IgnoreContrived t3 = new IgnoreContrived(21, "Hello", false, new HashSet<>(), null);
        IgnoreContrived t4 = new IgnoreContrived(19, "World", true, null, t3);

        testClean("""
                {
                  "name": "Peter",
                  "age": 23
                }
                """, t1);

        testClean("""
                {
                  "name": "John Doe",
                  "age": -1
                }
                """, t2);
        testClean("""
                {
                  "f1": 21
                }
                """, t3);
        testClean("""
                {
                  "f1": 19
                }
                """, t4);
        testClean("""
                {
                }
                """, new IgnoreTrivial());

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

    record ContrivedTree<T>(ContrivedTree<?> left, T node, ContrivedTree<?> right) implements JSONSerialisable{}

}
