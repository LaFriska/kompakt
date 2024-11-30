package com.friska.kompakt;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Classes implementing this interface allows Kompakt to search through field variables and serialise them into a
 * JSON string. This process is done by calling {@link JSONSerialisable#serialise()}. There are configurations available
 * in the form of trivial methods with a default return value, but should be overridden if such configurations should
 * be modified. Below are such configurations. (Read their documentation for more detail.)
 * <ul>
 *     <li>
 *         {@link JSONSerialisable#ignoredFields()}, returns an array of field names that should be ignored from
 *         serialisation.
 *     </li>
 *     <li>
 *         {@link JSONSerialisable#deepSerialise()}, returns whether fields from super classes should be serialised.
 *     </li>
 *     <li>
 *         {@link JSONSerialisable#serialiseIterablesAsArrays()}, returns whether fields that inherit {@link Iterable}
 *         should be serialised as JSON arrays.
 *     </li>
 *     <li>
 *         {@link JSONSerialisable#jsonAttributes()}, returns a list of attributes to be serialised.
 *     </li>
 * </ul>
 * Another configuration that does not come in the form of a default non-static method is
 * {@link JSONSerialisable#setIndentSize(int)}, which sets the global indent size of JSON serialisation using a single
 * static setter.
 */
public interface JSONSerialisable {

    /**
     * Sets a new indentation size for JSON serialisations.
     *
     * @param newSize the number of spaces for each indentation.
     */
    static void setIndentSize(int newSize) {
        JSONUtils.INDENT_SIZE = newSize;
    }

    /**
     * Fetches non-static fields from an object's class and converts them to instances of {@link Attribute}.
     *
     * @param obj the object to fetch the fields from.
     * @return a list of attributes representing these fields.
     */
    private static List<Attribute> fetchFieldsAsAttributes(Object obj) {
        return fieldToAttributes(obj.getClass().getDeclaredFields(), obj);
    }

    /**
     * Serialises an a JSON-serialisable object.
     *
     * @param obj      the object to serialise.
     * @param currSize current size of the indentation.
     * @param omitted  a set of names of omitted field variables.
     * @param <T>      an arbitrary type that extends {@link JSONSerialisable}.
     * @return a string representation of the serialised JSON object.
     * @throws IllegalAccessException
     */
    private static <T extends JSONSerialisable> String serialise(T obj, int currSize, Set<String> omitted)
            throws IllegalAccessException {

        //Fetches the attributes
        List<Attribute> attributes = new ArrayList<>();
        getAttributes(obj, obj.getClass(), attributes, obj.deepSerialise());

        //Initialise string builder
        StringBuilder sb = new StringBuilder();
        indent(sb, currSize, s -> s.append("{").append("\n"));

        //Iterate
        for (Attribute attribute : attributes) {
            String name = attribute.name();
            Object val = attribute.val();
            if (!omitted.contains(name)) {
                indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append(wrap(name)).append(": "));
                serialiseItem(currSize, val, sb, false);
                sb.append(",").append("\n");
            }
        }

        if (sb.charAt(sb.length() - 2) == ',')
            sb.delete(sb.length() - 2, sb.length() - 1);
        indent(sb, currSize, s -> s.append("}"));
        return sb.toString();
    }

    private static List<Attribute> fieldToAttributes(Field[] fields, Object obj) {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();
            for (Field field : fields) {
                if (!field.accessFlags().contains(AccessFlag.STATIC)) {
                    boolean canAccess = field.canAccess(obj);
                    field.setAccessible(true);
                    attributes.add(new Attribute(field.getName(), field.get(obj)));
                    field.setAccessible(canAccess);
                }
            }
            return attributes;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred.");
        }
    }

    /**
     * Fetches all attributes that will be serialised into the JSON string. By default, this means every non-static
     * field of an object. However, one may override {@link JSONSerialisable#jsonAttributes()} to explicitly denote
     * each attribute that should be serialised.
     *
     * @param obj          the object whose attributes are fetched.
     * @param clazz        class from which attributes are fetched (may be the class of obj, or superclasses of obj).
     * @param list         a list that will be mutated by this method to store the attributes.
     * @param getFieldDeep whether inheritted fields should be serialised as attributes.
     * @param <T>          an arbitrary inheriter of {@link JSONSerialisable}.
     * @see Attribute
     */
    private static <T extends JSONSerialisable> void
    getAttributes(T obj, Class<?> clazz, List<Attribute> list, boolean getFieldDeep) {
        if (!getFieldDeep) {
            if (clazz.equals(obj.getClass())) list.addAll(obj.jsonAttributes());
            else list.addAll(fieldToAttributes(clazz.getDeclaredFields(), obj));
            return;
        }

        Class<?> superClass = clazz.getSuperclass();
        getAttributes(obj, clazz, list, false);

        //Base case
        if (superClass == null) return;

        //Inductive case
        getAttributes(obj, superClass, list, true);

    }

    /**
     * Helper method to serialise a single value, which is either the value to field or another serialisable object.
     *
     * @param currSize current size of the indentation.
     * @param item     the item to serialise.
     * @param sb       current string builder used in the serialisation.
     */
    private static void serialiseItem(int currSize, Object item, StringBuilder sb, boolean indentAlways) {
        //Recursive call
        if (item instanceof JSONSerialisable s)
            sb.append(s.serialise(currSize + JSONUtils.INDENT_SIZE, s.ignoredFields()));

            //Base cases
        else if (item == null)
            if (indentAlways)
                indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append("null"));
            else
                sb.append("null");
        else if (item instanceof Number || item instanceof Boolean)
            if (indentAlways)
                indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append(item));
            else
                sb.append(item);
        else if (item.getClass().isArray()) {
            sb.append("[").append("\n");
            Object[] array = (Object[]) item;
            for (int i = 0; i < array.length; i++) {
                serialiseItem(currSize + JSONUtils.INDENT_SIZE, array[i], sb, true);
                if (i != array.length - 1) sb.append(",");
                sb.append("\n");
            }
            indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append("]"));
        } else if (item instanceof Iterable<?> iterable) {
            sb.append("[").append("\n");
            boolean flag = false;
            for (Object o : iterable) {
                if (flag) {
                    sb.append(",");
                    sb.append("\n");
                }
                serialiseItem(currSize + JSONUtils.INDENT_SIZE, o, sb, true);
                flag = true;
            }
            indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append("]"));
        } else if (indentAlways)
            indent(sb, currSize + JSONUtils.INDENT_SIZE,
                    s -> s.append(wrap(item.toString())));
        else
            sb.append(wrap(item.toString()));
    }

    private static void indent(StringBuilder sb, int indentSize, Consumer<StringBuilder> action) {
        sb.append(" ".repeat(Math.max(0, indentSize)));
        action.accept(sb);
    }

    private static String wrap(String str) {
        return "\"" + JSONUtils.sanitiseString(str) + "\"";
    }

    /**
     * By default, every field regardless of visibility or other tags is included in the serialisation process. To
     * explicitly omit certain field variables, this method should be overwritten returning an array of strings
     * representing names of field variables that should be omitted. Return null to dictate that no fields should be
     * ignored (all fields are included in the JSON serialisation process).
     *
     * @return an array of field variables that should be ignored by the serialisation process.
     */
    default String[] ignoredFields() {
        return null;
    }

    /**
     * By default, this interface only serialises non-inherited fields in the child class. In order to serialise
     * inherited ones too, this method should be overridden to return true, in which case every non-static fields,
     * including private ones, unless omitted by {@link JSONSerialisable#ignoredFields()}, will be serialised.
     *
     * @return whether inherited fields should be serialised.
     */
    default boolean deepSerialise() {
        return false;
    }

    /**
     * By default, fields that are instances of the {@link Iterable} interface will be serialised as JSON arrays.
     * Some examples of childrens of {@link Iterable} are {@link HashSet}, and {@link ArrayList}. If for whatever
     * reason this feature should be disabled, override this method and return false.
     *
     * @return whether instances of {@link Iterable} are serialised as arrays.
     */
    default boolean serialiseIterablesAsArrays() {
        return true;
    }

    /**
     * This method denotes the attributes that should be serialised into the JSON string. The default implementation
     * ensures that an attribute is serialised if and only if it is represented by a non-static field in the object
     * class. However, overriding this method may enable custom attributes to be serialised. Note that in this case,
     * an {@link Attribute} is simply a record holding a name, which is a string, and an arbitrary {@link Object}.<p>
     * <b>Please be adviced that</b> this method will not over-write {@link JSONSerialisable#deepSerialise()}, and if
     * {@link JSONSerialisable#deepSerialise()} is overridden to return true, with a custom implementation of this method,
     * inherited fields will <b>still be serialised</b>.
     *
     * @return a list of attributes to be serialised.
     */
    default List<Attribute> jsonAttributes() {
        return fetchFieldsAsAttributes(this);
    }

    /**
     * Serialises the object. Note that this method will only serialise fields declared in the class itself, not
     * inherited ones. Any fields that has the form of an array of objects will be serialised as a JSON array.
     * Primitive number types, or any fields that inherit {@link Number} will be serialised as a JSON number, and
     * booleans will be serialised as a JSON boolean. Any other form of objects will be serialised as strings by
     * calling {@link Object#toString()}. This method is recursive in nature, and note that any circular dependency
     * of fields may cause non-termination.
     *
     * @return a JSON-string representation of the object.
     */
    default String serialise() {
        return serialise(0, ignoredFields());
    }

    /**
     * Helper method for {@link JSONSerialisable#serialise()} used in the recursive calls.
     *
     * @param currSize the indentation to be used in the current depth level.
     * @param omitted  an omitted set of field names.
     */
    default String serialise(int currSize, String[] omitted) {
        try {
            HashSet<String> omittedFields = omitted == null ?
                    new HashSet<>() :
                    new HashSet<>(List.of(omitted));
            return serialise(this, currSize, omittedFields);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("An issue occurred on serialising an object to JSON.");
        }
    }

}
