import com.friska.AttributeNotFoundException;
import com.friska.AttributeTypeException;
import com.friska.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class extensively tests {@link JSONObject}.
 */
public class JSONObjectTest {

    /**
     * This method tests add, remove and simple getters.
     */
    @Test
    public void testBasicOperations() {
        JSONObject o = new JSONObject();

        //Test adding attributes
        o.addAttribute("name", "John Doe");
        o.addAttribute("age", 30);
        o.addAttribute("isActive", true);
        o.addAttribute("nestedObject", new JSONObject());
        o.addAttribute("array", new String[]{"item1", "item2"});

        //Test retrieving attributes
        assertEquals("John Doe", o.getString("name"));
        assertEquals(30, o.getNumber("age"));
        assertEquals(true, o.getBool("isActive"));
        assertNotNull(o.getJSONObject("nestedObject"));
        assertArrayEquals(new String[]{"item1", "item2"}, o.getArray("array"));

        //Test contains method
        assertTrue(o.contains("name"));
        assertTrue(o.contains("age"));
        assertFalse(o.contains("nonExistentAttribute"));

        //Test removeAttribute
        Object removed = o.removeAttribute("name");
        assertEquals("John Doe", removed);
        assertFalse(o.contains("name"));

        //Validate removal
        assertThrows(AttributeNotFoundException.class, () -> o.getItem("name"));
        assertThrows(AttributeNotFoundException.class, () -> o.getString("name"));

        //Test removing a non-existent attribute
        assertThrows(AttributeNotFoundException.class, () -> o.removeAttribute("nonExistentAttribute"));

        //Test adding a duplicate attribute
        o.addAttribute("duplicateTest", 42);
        assertThrows(IllegalArgumentException.class, () -> o.addAttribute("duplicateTest", 100));

        //Test null and edge cases
        o.addAttribute("nullableAttribute", null);
        assertNull(o.getItem("nullableAttribute"));
        assertNull(o.getString("nullableAttribute"));
        assertNull(o.getBool("nullableAttribute"));
        assertNull(o.getJSONObject("nullableAttribute"));
        assertNull(o.getNumber("nullableAttribute"));
        assertNull(o.getArray("nullableAttribute"));

        //Test invalid type access
        assertThrows(AttributeTypeException.class, () -> o.getString("age")); // age is not String
        assertThrows(AttributeTypeException.class, () -> o.getBool("age"));   // age is not a Boolean
        assertThrows(AttributeTypeException.class, () -> o.getArray("age"));  // age is not an Array

        // est object validity after multiple operations
        assertFalse(o.isEmpty());
        o.removeAttribute("age");
        o.removeAttribute("isActive");
        o.removeAttribute("nestedObject");
        o.removeAttribute("array");
        o.removeAttribute("nullableAttribute");
        assertFalse(o.isEmpty());
        o.removeAttribute("duplicateTest");
        assertTrue(o.isEmpty());
    }

    @Test
    public void testEquivalence(){

        JSONObject o1 = new JSONObject();
        JSONObject c1 = new JSONObject();
        c1.addAttribute("name", "Peter");
        c1.addAttribute("age", 18);
        c1.addAttribute("isEmployee", true);

        o1.addAttribute("animal", "Cat");
        o1.addAttribute("age", 5);
        o1.addAttribute("adopted", false);
        o1.addAttribute("children", new String[]{"child1", "child2", "child3"});
        o1.addAttribute("zoo", null);
        o1.addAttribute("caretaker", c1);

        JSONObject o2 = new JSONObject();
        JSONObject c2 = new JSONObject();
        c2.addAttribute("age", 18);
        c2.addAttribute("isEmployee", true);
        c2.addAttribute("name", "Peter");

        o2.addAttribute("animal", "Cat");
        o2.addAttribute("zoo", null);
        o2.addAttribute("caretaker", c2);
        o2.addAttribute("adopted", false);
        o2.addAttribute("children", new String[]{"child1", "child2", "child3"});
        o2.addAttribute("age", 5);

        assertTrue(o1.equals(o2));
        c2.removeAttribute("isEmployee");
        assertFalse(o1.equals(o2));
        c1.addAttribute("isEmployee", false);
        assertFalse(o1.equals(o2));
        c2.removeAttribute("isEmployee");
        c1.addAttribute("isEmployee", true);
        assertTrue(o1.equals(o2));
    }

    @Test
    public void testTypes(){

        JSONObject o = new JSONObject();
        JSONObject careTaker = new JSONObject();
        careTaker.addAttribute("name", "Peter");
        careTaker.addAttribute("age", 18);
        careTaker.addAttribute("isEmployee", true);

        o.addAttribute("animal", "Cat");
        o.addAttribute("age", 5);
        o.addAttribute("adopted", false);
        o.addAttribute("children", new String[]{"child1", "child2", "child3"});
        o.addAttribute("zoo", null);
        o.addAttribute("caretaker", careTaker);

    }

}
