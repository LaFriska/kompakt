import com.friska.Attribute;
import com.friska.JSONSerialisable;
import org.junit.Test;

import java.util.ArrayList;
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

        testClean("""
                {
                  "name": "Peter Sladkovic",
                  "age": 23
                }
                """, new Person("Peter", "Sladkovic", 23));

    }

    @Test
    public void testAverage(){

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

    }

    public <T extends JSONSerialisable> void testClean(String expected, T obj){
        assertEquals(Utils.strip(expected), Utils.strip(obj.serialise()));
    }
}
