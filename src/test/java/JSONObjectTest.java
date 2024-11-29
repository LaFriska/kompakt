import com.friska.kompakt.exceptions.AttributeNotFoundException;
import com.friska.kompakt.exceptions.IllegalTypeException;
import com.friska.kompakt.JSONObject;
import org.jetbrains.annotations.NotNull;
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
        assertThrows(IllegalTypeException.class, () -> o.getString("age")); // age is not String
        assertThrows(IllegalTypeException.class, () -> o.getBool("age"));   // age is not a Boolean
        assertThrows(IllegalTypeException.class, () -> o.getArray("age"));  // age is not an Array

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

    /**
     * Tests equivalence for simple cases.
     */
    @Test
    public void testEquivalenceTrivial(){

        JSONObject empty1 = new JSONObject();
        JSONObject empty2 = new JSONObject();
        JSONObject triv1 = new JSONObject();
        JSONObject triv2 = new JSONObject();
        triv1.addAttribute("random", null);
        triv2.addAttribute("random", null);

        equalRigorous(empty1, empty2, true);
        equalRigorous(empty1, triv2, false);
        equalRigorous(triv1, triv2, true);
        triv2.removeAttribute("random");
        equalRigorous(triv1, triv2, false);
    }

    /**
     * Tests equivalence for moderate cases.
     */
    @Test
    public void testEquivalence(){

        //Basic test

        JSONObject child1 = new JSONObject();
        child1.addAttribute("field1", "Blah");
        child1.addAttribute("field2", 30);
        child1.addAttribute("field3", null);

        JSONObject child2 = new JSONObject();
        child2.addAttribute("field1", "Blah");
        child2.addAttribute("field2", 30);
        child2.addAttribute("field3", null);

        JSONObject o1 = new JSONObject();
        JSONObject o2 = new JSONObject();

        o1.addAttribute("parentField", true);
        o2.addAttribute("parentField", true);
        o1.addAttribute("child", child1);
        o2.addAttribute("child", child2);

        equalRigorous(child1, child2, true);
        equalRigorous(o1, o2, true);

        //Tests removal and add

        child1.removeAttribute("field1");
        equalRigorous(child1, child2, false);
        equalRigorous(o1, o2, false);
        child1.addAttribute("field1", "Blah");
        equalRigorous(child1, child2, true);
        equalRigorous(o1, o2, true);
        child2.addAttribute("field4", null);
        equalRigorous(child1, child2, false);
        equalRigorous(o1, o2, false);

        //Tests same field names but values differ
        o1 = new JSONObject();
        o2 = new JSONObject();
        JSONObject o3 = new JSONObject();
        JSONObject o4 = new JSONObject();

        o1.addAttribute("field1", null);
        o1.addAttribute("field2", 3.14);
        o1.addAttribute("field3", true);
        o1.addAttribute("field4", "Hello World!");

        o2.addAttribute("field1", null);
        o2.addAttribute("field2", 3.145);
        o2.addAttribute("field3", true);
        o2.addAttribute("field4", "Hello World!");
        equalRigorous(o1, o2, false);

        JSONObject childAlt = new JSONObject();
        childAlt.addAttribute("field1", "Blah!");
        childAlt.addAttribute("field2", 30);
        childAlt.addAttribute("field3", null);

        o1 = new JSONObject();
        o1.addAttribute("field", "Hello World!");
        o1.addAttribute("child", child1);
        o2 = new JSONObject();
        o2.addAttribute("field", "Hello World!");
        o2.addAttribute("child", childAlt);

        equalRigorous(o1, o2, false);

        childAlt.removeAttribute("field1");
        childAlt.addAttribute("field1", "Blah");

        equalRigorous(o1, o2, true);
    }

    /**
     * Tests equivalence for complicated cases.
     */
    @Test
    public void testEquivalenceComplicated() {

        JSONObject o1 = new JSONObject();
        o1.addAttribute("level1", "data1");

        JSONObject o2 = new JSONObject();
        o2.addAttribute("level1", "data1");

        JSONObject complex1 = new JSONObject();
        JSONObject complex2 = new JSONObject();

        Object[] array1 = new Object[]{"one", 2, true, o1};
        Object[] array2 = new Object[]{"one", 2, true, o2};

        complex1.addAttribute("arrayField", array1);
        complex2.addAttribute("arrayField", array2);
        complex1.addAttribute("name", "Blah");
        complex2.addAttribute("name", "Blah");

        equalRigorous(o1, o2, true);
        equalRigorous(complex1, complex2, true);

        //modify one of the nested objects
        o1.addAttribute("level2", "data2");
        equalRigorous(o1, o2, false);
        equalRigorous(complex1, complex2, false);

        o2.addAttribute("level2", "data2");
        equalRigorous(o1, o2, true);
        equalRigorous(complex1, complex2, true);

        //change an array element
        array2[1] = 3; // Modify the integer in the array
        equalRigorous(complex1, complex2, false);

        array2[1] = 2; //revert the change
        equalRigorous(complex1, complex2, true);

        //add extra fields to the root objects
        complex1.addAttribute("extraField", 123);
        equalRigorous(complex1, complex2, false);

        complex2.addAttribute("extraField", 123);
        equalRigorous(complex1, complex2, true);

        //test deeply nested structures
        JSONObject deepNested1 = new JSONObject();
        JSONObject deepNested2 = new JSONObject();
        JSONObject middle1 = new JSONObject();
        JSONObject middle2 = new JSONObject();

        middle1.addAttribute("middleLevel", o1);
        middle2.addAttribute("middleLevel", o2);
        deepNested1.addAttribute("deepLevel", middle1);
        deepNested2.addAttribute("deepLevel", middle2);

        equalRigorous(deepNested1, deepNested2, true);

        //change a deep nested value
        o1.addAttribute("uniqueField", "uniqueValue");
        equalRigorous(deepNested1, deepNested2, false);

        o2.addAttribute("uniqueField", "uniqueValue");
        equalRigorous(deepNested1, deepNested2, true);

        //arrays of objects with different orders
        Object[] array3 = new Object[]{o1, middle1, "randomValue"};
        Object[] array4 = new Object[]{middle1, o1, "randomValue"};

        JSONObject orderSensitive1 = new JSONObject();
        JSONObject orderSensitive2 = new JSONObject();

        orderSensitive1.addAttribute("sensitiveArray", array3);
        orderSensitive2.addAttribute("sensitiveArray", array4);

        equalRigorous(orderSensitive1, orderSensitive2, false);

        //ensure adding/removing attributes in deeply nested structures reflects properly
        middle1.addAttribute("additionalData", 42);
        equalRigorous(deepNested1, deepNested2, false);

        middle2.addAttribute("additionalData", 42);
        equalRigorous(deepNested1, deepNested2, true);
    }


    /**
     * Given two instances of {@link JSONObject} tests if they are equal both ways, and tests their equivalence on
     * themselves.
     * @param areEqual whether the two objects are expected to be equal.
     */
    private void equalRigorous(@NotNull JSONObject o1, @NotNull JSONObject o2, boolean areEqual){
        assertTrue(o1.equals(o1));
        assertTrue(o2.equals(o2));
        if(areEqual){
            assertTrue(o1.equals(o2));
            assertTrue(o2.equals(o1));
        }else{
            assertFalse(o1.equals(o2));
            assertFalse(o2.equals(o1));
        }
    }


    /**
     * Tests retrieving values of the right type.
     */
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

        //Types are correct
        assertEquals("Cat", o.getString("animal"));
        assertEquals(5, o.getNumber("age"));
        assertEquals(false, o.getBool("adopted"));
        assertEqualsArray(new String[]{"child1", "child2", "child3"}, o.getArray("children"));
        assertNull(o.getNumber("zoo"));
        assertNull(o.getString("zoo"));
        assertNull(o.getJSONObject("zoo"));
        assertNull(o.getBool("zoo"));
        assertEquals(careTaker, o.getJSONObject("caretaker"));

        //Types are wrong
        assertThrows(IllegalTypeException.class, () -> o.getString("age"));
        assertThrows(IllegalTypeException.class, () -> o.getString("adopted"));
        assertThrows(IllegalTypeException.class, () -> o.getString("children"));

        assertThrows(IllegalTypeException.class, () -> o.getNumber("adopted"));
        assertThrows(IllegalTypeException.class, () -> o.getNumber("animal"));
        assertThrows(IllegalTypeException.class, () -> o.getNumber("caretaker"));

        assertThrows(IllegalTypeException.class, () -> o.getBool("animal"));
        assertThrows(IllegalTypeException.class, () -> o.getBool("age"));
        assertThrows(IllegalTypeException.class, () -> o.getBool("children"));

        assertThrows(IllegalTypeException.class, () -> o.getArray("age"));
        assertThrows(IllegalTypeException.class, () -> o.getArray("adopted"));
        assertThrows(IllegalTypeException.class, () -> o.getArray("caretaker"));

        assertThrows(IllegalTypeException.class, () -> o.getJSONObject("age"));
        assertThrows(IllegalTypeException.class, () -> o.getJSONObject("adopted"));
        assertThrows(IllegalTypeException.class, () -> o.getJSONObject("animal"));
    }

    public void assertEqualsArray(Object[] expected, Object[] actual){
        if(expected == null && actual == null) return;
        if(expected == null || actual == null) fail();
        if(actual.length != expected.length) fail();
        for (int i = 0; i < actual.length; i++)
            if(!expected[i].equals(actual[i])) fail();
    }

}
