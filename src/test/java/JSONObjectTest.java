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
        JSONObject jsonObject = new JSONObject();

        // Test adding attributes
        jsonObject.addAttribute("name", "John Doe");
        jsonObject.addAttribute("age", 30);
        jsonObject.addAttribute("isActive", true);
        jsonObject.addAttribute("nestedObject", new JSONObject());
        jsonObject.addAttribute("array", new String[]{"item1", "item2"});

        // Test retrieving attributes
        assertEquals("John Doe", jsonObject.getString("name"));
        assertEquals(30, jsonObject.getNumber("age"));
        assertEquals(true, jsonObject.getBool("isActive"));
        assertNotNull(jsonObject.getJSONObject("nestedObject"));
        assertArrayEquals(new String[]{"item1", "item2"}, jsonObject.getArray("array"));

        // Test contains method
        assertTrue(jsonObject.contains("name"));
        assertTrue(jsonObject.contains("age"));
        assertFalse(jsonObject.contains("nonExistentAttribute"));

        // Test removeAttribute
        Object removed = jsonObject.removeAttribute("name");
        assertEquals("John Doe", removed);
        assertFalse(jsonObject.contains("name"));

        // Validate removal effects
        assertThrows(AttributeNotFoundException.class, () -> jsonObject.getItem("name"));
        assertThrows(AttributeNotFoundException.class, () -> jsonObject.getString("name"));

        // Test removing a non-existent attribute
        assertThrows(AttributeNotFoundException.class, () -> jsonObject.removeAttribute("nonExistentAttribute"));

        // Test adding a duplicate attribute
        jsonObject.addAttribute("duplicateTest", 42);
        assertThrows(IllegalArgumentException.class, () -> jsonObject.addAttribute("duplicateTest", 100));

        // Test null and edge cases
        jsonObject.addAttribute("nullableAttribute", null);
        assertNull(jsonObject.getItem("nullableAttribute"));
        assertNull(jsonObject.getString("nullableAttribute"));
        assertNull(jsonObject.getBool("nullableAttribute"));
        assertNull(jsonObject.getJSONObject("nullableAttribute"));
        assertNull(jsonObject.getNumber("nullableAttribute"));
        assertNull(jsonObject.getArray("nullableAttribute"));

        // Test invalid type access
        assertThrows(AttributeTypeException.class, () -> jsonObject.getString("age")); // age is a Number, not String
        assertThrows(AttributeTypeException.class, () -> jsonObject.getBool("age"));   // age is not a Boolean
        assertThrows(AttributeTypeException.class, () -> jsonObject.getArray("age"));  // age is not an Array

        // Test object validity after multiple operations
        assertFalse(jsonObject.isEmpty());
        jsonObject.removeAttribute("age");
        jsonObject.removeAttribute("isActive");
        jsonObject.removeAttribute("nestedObject");
        jsonObject.removeAttribute("array");
        jsonObject.removeAttribute("nullableAttribute");
        assertFalse(jsonObject.isEmpty());
        jsonObject.removeAttribute("duplicateTest");
        assertTrue(jsonObject.isEmpty());
    }

}
