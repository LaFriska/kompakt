package com.friska.kompakt;

import com.friska.kompakt.annotations.DeepSerialise;
import com.friska.kompakt.annotations.Ignored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Classes implementing this interface allows Kompakt to search through field variables and serialise them into a
 * JSON string. This process is done by calling {@link JSONSerialisable#serialise()}. There are configurations available
 * in the form annotations, or of methods with a default return value, but should be overridden if such configurations should
 * be modified. Below are such configurations.
 * <ul>
 *     <li>
 *         Ignoring fields - to ignore fields from the serialisation process, annotate the given field with {@link Ignored}
 *     </li>
 *     <li>
 *         Deep serialise - to serialise inherited fields, annotate the class with {@link DeepSerialise}.
 *     </li>
 *     <li>
 *         Custom JSON attributes - to serialise the class into a custom set of attributes, override
 *         {@link JSONSerialisable#jsonAttributes()}.
 *     </li>
 *     <li>
 *         Custom indent sizes - to customise the JSON indent size, call the static setter
 *         {@link JSONSerialisable#setIndentSize(int)}.
 *     </li>
 * </ul>
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
        return JSONUtils.fieldToAttributes(obj.getClass().getDeclaredFields(), obj);
    }

    /**
     * Serialises an a JSON-serialisable object.
     *
     * @param obj      the object to serialise.
     * @param currSize current size of the indentation.
     * @param omitted  a set of names of omitted field variables.
     * @param <T>      an arbitrary type that extends {@link JSONSerialisable}.
     * @return a string representation of the serialised JSON object.
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
            else list.addAll(JSONUtils.fieldToAttributes(clazz.getDeclaredFields(), obj));
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

        if (item instanceof JSONSerialisable s)
            sb.append(s.serialise(currSize + JSONUtils.INDENT_SIZE, s.ignoredFields()));

        else if (item == null)
            handle(currSize, sb, indentAlways, "null");
        else if (item instanceof Number || item instanceof Boolean)
            handle(currSize, sb, indentAlways, item.toString());
        else if (item.getClass().isArray())
            handleArray(currSize, (Object[]) item, sb);
        else if (item instanceof Iterable<?> iterable)
            handleIterable(currSize, sb, iterable);
        else
            handle(currSize, sb, indentAlways, wrap(item.toString()));
    }

    private static void handleArray(int currSize, Object[] item, StringBuilder sb) {
        sb.append("[").append("\n");
        Object[] array = item;
        for (int i = 0; i < array.length; i++) {
            serialiseItem(currSize + JSONUtils.INDENT_SIZE, array[i], sb, true);
            if (i != array.length - 1) sb.append(",");
            sb.append("\n");
        }
        indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append("]"));
    }

    private static void handleIterable(int currSize, StringBuilder sb, Iterable<?> iterable) {
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
    }

    private static void handle(int currSize, StringBuilder sb, boolean indentAlways, String str) {
        if (indentAlways)
            indent(sb, currSize + JSONUtils.INDENT_SIZE, s -> s.append(str));
        else
            sb.append(str);
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
     * By default, Kompakt only serialises non-inherited fields in the child class. In order to serialise
     * inherited ones too, this method should be overridden to return true, or the class of the object being serialised
     * should be annotated with {@link DeepSerialise}, in which case every non-static fields,
     * including private ones, unless ignored, will be serialised.
     *
     * @return whether inherited fields should be serialised.
     */
    default boolean deepSerialise() {
        return this.getClass().isAnnotationPresent(DeepSerialise.class);
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
