# Introduction

Kompakt is a lightweight, easy-to-use and rigorous Java library aimed to provide serialisation, deserialisation and
parsing of the JavaScript Object Notation (JSON) data-interchange format. Kompakt is designed to provide sufficient
control to the developer over how their classes interact with JSON, while also keeping the process simple and beginner
friendly.

## About JSON

JSON is a data-exchange format commongly used also for storing structured, object-oriented data
in a readable, C-styled syntax. JSON is incredibly simple in nature, and
is generated by a context-free grammar of very few production rules. For more information
on the particularities of JSON, see [the official JSON website](https://www.json.org/json-en.html).

# Installation

**Using Library as Jar**

1. Head to [this link](https://github.com/LaFriska/kompakt/releases) and download the latest release (jar file). 
2. Follow one of the thousands of resources online to use the jar file in your project.
If you are using IntelliJ in particular, go to `File -> Project Structure -> Libraries`, then click
the plus (+) icon at the top. There you can the Kompakt
jar file into your project.

**Using Library as Maven Dependency**

Coming Soon!

# Documentation

Kompakt can do two important things:
1. Serialise a Java object to JSON, and
2. Parse and deserialise a JSON string into Java.

## Serialisation

To serialise a Java object to JSON, edit the class of that object to
implement `JSONSerialisable`, then simply call `serialise()` from the object.
```java
import com.friska.kompakt.JSONSerialisable;
public class Person implements JSONSerialisable {
    
    String name;
    
    int age;
    
    boolean isHappy;
    
    public Person(String name, int age, boolean isHappy){
        this.name = name;
        this.age = age;
        this.isHappy = isHappy;
    }
    
}
```
Then, the following code
```java
public class Main {

    public static void main(String[] args) {
        Person john = new Person("John Doe", 23, true);
        System.out.println(john.serialise());
    }

}
```
will print the corresponding JSON string into the console.
```json
{
  "name": "John Doe",
  "age": 23,
  "isHappy": true
}
```
### Ignoring fields

In order to tell Kompakt to ignore certain fields, override `JSONSerialisable#ignoredFields()` to return
an array of field names to ignore. Using the example above, if we wish to ignore `isHappy`,
then we would write the class as follows

```java
import com.friska.kompakt.JSONSerialisable;
public class Person implements JSONSerialisable {

    String name;

    int age;

    boolean isHappy;

    public Person(String name, int age, boolean isHappy){
        this.name = name;
        this.age = age;
        this.isHappy = isHappy;
    }

    //Ignores the "isHappy" field.
    @Override
    public String[] ignoredFields() {
        return new String[]{"isHappy"};
    }
}
```
The resulting JSON will be
```json
{
  "name": "John Doe",
  "age": 23
}
```

### Deep Serialise

By default, Kompakt will not serialise inherited fields. Take the following
example.

```java
import com.friska.kompakt.JSONSerialisable;

public class Student extends Person implements JSONSerialisable {
    
    private int grades;
    
    public Student(String name, int age, boolean isHappy, int grades) {
        super(name, age, isHappy);
        this.grades = grades;
    }
    
}
```
```java
public class Main {

    public static void main(String[] args) {
        Student alice = new Student("Alice Lee", 15, false, 32);
        System.out.println(alice.serialise());
    }

}
```
The resulting JSON is
```json
{
  "grades": 32
}
```
In order to allow Kompakt to serialise inherited fields. we override 
`JSONSerialisable#deepSerialise()` and return true, as follows.
```java
import com.friska.kompakt.JSONSerialisable;

public class Student extends Person implements JSONSerialisable {

    private int grades;

    public Student(String name, int age, boolean isHappy, int grades) {
        super(name, age, isHappy);
        this.grades = grades;
    }

    //Allows inherited fields to be serialised.
    @Override
    public boolean deepSerialise() {
        return true;
    }
}
```
Now, the resulting JSON is
```json
{
  "grades": 32,
  "name": "Alice Lee",
  "age": 15,
  "isHappy": false
}
```

### Customisation

Ignoring fields often does not provide enough control to the developer
over what the resulting JSON should look like. For full customisation, 
we override `JSONSerialisable#jsonAttributes()` to return a list of 
custom instances of `Attribute` (which are simply key-value pairs) to 
represent a custom list of elements in the resulting JSON. 

Below is an example.

```java
public class Person implements JSONSerialisable {

    String firstName;

    String lastName;

    String mother;

    String father;

    String grandma;

    String grandpa;

    int age;

    int bDay;

    int bMonth;

    int bYear;

    public Person(String firstName, String lastName, String mother, String father,
                  String grandma, String grandpa, int age, int bDay,
                  int bMonth, int bYear) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mother = mother;
        this.father = father;
        this.grandma = grandma;
        this.grandpa = grandpa;
        this.age = age;
        this.bDay = bDay;
        this.bMonth = bMonth;
        this.bYear = bYear;
    }

    @Override
    public List<Attribute> jsonAttributes() {
        ArrayList<Attribute> att = new ArrayList<>();
        att.add(new Attribute("name", firstName + " " + lastName));
        att.add(new Attribute("family", new String[]{mother, father, grandma, grandpa}));
        att.add(new Attribute("birthday", bDay + "/" + bMonth + "/" + bYear));
        att.add(new Attribute("age", age));
        return att;
    }
}
```

```java
public class Main {

    public static void main(String[] args) {
        Person bob = new Person("Bob", 
                "Douglas", 
                "Mary", 
                "Sergei", 
                "Liz", 
                "Boris", 
                3, 
                10, 
                12, 
                2020);
        System.out.println(bob.serialise());
    }
}
```

Without overriding `jsonAttributes()`, all 10 fields will be serialised in a
clumsy manner. However, with the override, the resulting JSON is
```json
{
  "name": "Bob Douglas",
  "family": [
    "Mary",
    "Sergei",
    "Liz",
    "Boris"
  ],
  "birthday": "10/12/2020",
  "age": 3
}
```
### Other features
Some less important yet notable features
1. `JSONSerialisable#serialiseIterablesAsArrays()`, which by default returns
true, it serialises any children of Java's `Iterable` class into a JSON
array. To disable this feature, as usual, override it and return false.
2. `JSONSerialisable#setIndentSize(int)`, which sets the size of an indentation
in a serialised JSON string.

## Deserialisation

Kompakt is able to parse and deserialise a well-formatted JSON string.
As specified in the official JSON documentations, a JSON string may represent
**6** different types of
data, two of which are recursively defined. Below is a table showing each
data type, and the outcome of Kompakt's deserialisation process.

| Type    | Outcome                                                       |
|---------|---------------------------------------------------------------|
| null    | `null`                                                        |
| String  | `String` instance                                             |
| Boolean | `Boolean` instance                                            |
| Number  | Either `Float`, `Double`, `Integer`, or `BigDecimal` instance |
| Object  | `JSONObject` instance                                         |
| Array   | `Object[]` instance                                           |

Note that the developer may choose which type of number the parser should use. However,
if a number represented in a JSON string cannot be converted to the
specified type, an exception will occur. This is especially a problem when 
the integer type is chosen.

In order to deserialise to an arbitrary `Object` instance, call `JSONParser#parse(String)`, which
would use `Float` as a default number type. Alternatively, using the overloaded method
`JSONParser#parse(String, NumberType)` will allow a specific choice of number type to be used.
(`NumberType` is an enumerator of `FLOAT`, `INT`, `DOUBLE`, and `BIGDECIMAL`). Below
is an example.

