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
        assertThrows(AttributeTypeException.class, () -> o.getString("age")); // age is a Number, not String
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

}
