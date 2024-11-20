package com.friska;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classes implementing this interface allows Kompakt to search through field variables and serialise them into a
 * JSON string. This process is done by calling {@link JSONSerialisable#serialise()}. The programme will interpret the
 * variables at its own discretion, for more control over the serialisation process, classes should instead extend
 * {@link JSONSerialiser}. This interface does however, provide control over which field variables may be omitted.
 * To do so, override {@link JSONSerialisable#ignoredFields()} and return a string of ignored field names.
 */
public interface JSONSerialisable {

    /**
     * The size of indentation in the resulting JSON string.
     */
    int INDENT_SIZE = 2;

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

    private static <T extends JSONSerialisable> String serialise(T obj, int currSize, Set<String> omitted)
                                                                                throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        indent(sb, currSize);
        sb.append("{").append("\n");
        for (Field field : fields) {
            if(!omitted.contains(field.getName())){
                boolean canAccess = field.canAccess(obj);
                field.setAccessible(true);
                Object item = field.get(obj);
                indent(sb, currSize + INDENT_SIZE);
                sb.append(wrap(field.getName())).append(": ");
                serialiseItem(currSize, item, sb);
                sb.append(",").append("\n");
                field.setAccessible(canAccess);
            }
        }
        if(sb.charAt(sb.length() - 2) == ',')
            sb.delete(sb.length() - 2, sb.length() - 1);
        indent(sb, currSize);
        sb.append("}");
        return sb.toString();
    }

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
            indent(sb, currSize + INDENT_SIZE);
            sb.append("]");
        }
        else
            sb.append(wrap(item.toString()));
    }

    private static void indent(StringBuilder sb, int indentSize){
        sb.append(" ".repeat(Math.max(0, indentSize)));
    }

    private static String wrap(String str){
        return "\"" + str + "\"";
    }

}
