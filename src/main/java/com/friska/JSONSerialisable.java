package com.friska;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.friska.JSONSettings.*;

/**
 * Classes implementing this interface allows Kompakt to search through field variables and serialise them into a
 * JSON string. This process is done by calling {@link JSONSerialisable#serialise()}. The programme will interpret the
 * variables at its own discretion, for more control over the serialisation process, classes should instead extend
 * {@link JSONSerialiser}. This interface does however, provide control over which field variables may be omitted.
 * To do so, override {@link JSONSerialisable#ignoredFields()} and return a string of ignored field names.
 */
public interface JSONSerialisable {

    /**
     * Sets a new indentation size for JSON serialisations.
     * @param newSize the number of spaces for each indentation.
     */
    static void setIndentSize(int newSize){
        INDENT_SIZE = newSize;
    }

    /**
     * By default, every field regardless of visibility or other tags is included in the serialisation process. To
     * explicitly omit certain field variables, this method should be overwritten returning an array of strings
     * representing names of field variables that should be omitted. Return null to dictate that no fields should be
     * ignored (all fields are included in the JSON serialisation process).
     * @return an array of field variables that should be ignored by the serialisation process.
     */
    default String[] ignoredFields(){
        return null;
    }

    /**
     * By default, this interface only serialises non-inherited fields in the child class. In order to serialise
     * inherited ones too, this method should be overridden to return true, in which case every non-static fields,
     * including private ones, unless omitted by {@link JSONSerialisable#ignoredFields()}, will be serialised.
     * @return whether inherited fields should be serialised.
     */
    default boolean deepSerialise(){
        return false;
    }

    /**
     * Serialises the object. Note that this method will only serialise fields declared in the class itself, not
     * inherited ones. Any fields that has the form of an array of objects will be serialised as a JSON array.
     * Primitive number types, or any fields that inherit {@link Number} will be serialised as a JSON number, and
     * booleans will be serialised as a JSON boolean. Any other form of objects will be serialised as strings by
     * calling {@link Object#toString()}. This method is recursive in nature, and note that any circular dependency
     * of fields may cause non-termination.
     * @return a JSON-string representation of the object.
     */
    default String serialise(){
        return serialise(0, ignoredFields());
    }

    /**
     * Helper method for {@link JSONSerialisable#serialise()} used in the recursive calls.
     * @param currSize the indentation to be used in the current depth level.
     * @param omitted an omitted set of field names.
     * */
    default String serialise(int currSize, String[] omitted){
        try {
            HashSet<String> omittedFields = omitted == null ?
                    new HashSet<>() :
                    new HashSet<>(List.of(omitted));
            return serialise(this, currSize, omittedFields);
        }catch (IllegalAccessException e){
            e.printStackTrace();
            throw new RuntimeException("An issue occurred on serialising an object to JSON.");
        }
    }

    /**
     * Serialises an a JSON-serialisable object.
     * @param obj the object to serialise.
     * @param currSize current size of the indentation.
     * @param omitted a set of names of omitted field variables.
     * @return a string representation of the serialised JSON object.
     * @param <T> an arbitrary type that extends {@link JSONSerialisable}.
     * @throws IllegalAccessException
     */
    private static <T extends JSONSerialisable> String serialise(T obj, int currSize, Set<String> omitted)
                                                                                      throws IllegalAccessException {

        //Fetches the fields
        List<Field> fields = new ArrayList<>();
        getFields(obj.getClass(), fields, obj.deepSerialise());

        //Initialise string builder
        StringBuilder sb = new StringBuilder();
        indent(sb, currSize, s -> s.append("{").append("\n"));

        //Iterate
        for (Field field : fields) {
            if(!omitted.contains(field.getName()) && !field.accessFlags().contains(AccessFlag.STATIC)){
                boolean canAccess = field.canAccess(obj);
                field.setAccessible(true);
                Object item = field.get(obj);
                indent(sb, currSize + INDENT_SIZE, s -> s.append(wrap(field.getName())).append(": "));
                serialiseItem(currSize, item, sb);
                sb.append(",").append("\n");
                field.setAccessible(canAccess);
            }
        }

        if(sb.charAt(sb.length() - 2) == ',')
            sb.delete(sb.length() - 2, sb.length() - 1);
        indent(sb, currSize, s -> s.append("}"));
        return sb.toString();
    }


    /**
     * Fetches all required fields to be serialised given an object, and adds them to a given list.
     * @param clazz the class from which the fields are fetched.
     * @param getFieldDeep whether the programme should fetch inherited fields. When this parameter is true, this
     *                     method becomes recursive.
     * @param list initially an empty list, fields will be added to this list.
     */
    private static void getFields(Class<?> clazz, List<Field> list, boolean getFieldDeep){

        if(!getFieldDeep){
            list.addAll(List.of(clazz.getDeclaredFields()));
            return;
        }

        Class<?> superClass = clazz.getSuperclass();
        getFields(clazz, list, false);

        //Base case
        if(superClass == null) return;

        //Inductive case
        getFields(superClass, list, true);
    }

    /**
     * Helper method to serialise a single item, which is either a field or another serialisable object.
     * @param currSize current size of the indentation.
     * @param item the item to serialise.
     * @param sb current string builder used in the serialisation.
     */
    private static void serialiseItem(int currSize, Object item, StringBuilder sb) {
        //Recursive call
        if(item instanceof JSONSerialisable serialisable)
            sb.append(serialisable.serialise(currSize + INDENT_SIZE, serialisable.ignoredFields()));

        //Base cases
        else if(item == null)
            sb.append("null");
        else if(item instanceof Number || item instanceof Boolean)
            sb.append(item);
        else if(item.getClass().isArray()){
            sb.append("[").append("\n");
            Object[] array = (Object[]) item;
            for (int i = 0; i < array.length; i++) {
                serialiseItem(currSize + INDENT_SIZE, array[i], sb);
                if(i != array.length - 1) sb.append(",");
                sb.append("\n");
            }
            indent(sb, currSize + INDENT_SIZE, s -> s.append("]"));
        }
        else
            sb.append(wrap(item.toString()));
    }

    private static void indent(StringBuilder sb, int indentSize, Consumer<StringBuilder> action){
        sb.append(" ".repeat(Math.max(0, indentSize)));
        action.accept(sb);
    }

    private static String wrap(String str){
        return "\"" + str + "\"";
    }

}
