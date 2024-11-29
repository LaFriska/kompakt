package com.friska;

import com.friska.exceptions.AttributeNotFoundException;
import com.friska.exceptions.IllegalTypeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class represents an arbitrary object that may be represented by JSON. It may be considered a "free" object
 * where attributes can be added and removed at runtime. An attribute is a String-Object pair, where an object is
 * uniquely identified by a unique name, similar to fields in an arbitrary Java class. Not only does this class
 * keep track of attributes, it must also keep track of the order of the attributes, hence attributes are stored
 * both in a {@link HashMap} for constant time access, and also {@link List} for ordering.<p>
 * The result of the JSON deserialisation process is also an instance of this class.
 * @see Attribute
 */
public class JSONObject implements JSONSerialisable{

    /**
     * A list that tracks the ordering of attributes, as well as used for JSON serialisation.
     * @see JSONSerialisable
     */
    private final List<Attribute> attributeList = new ArrayList<>();

    /**
     * A hash map storing each attribute uniquely identified by its name, for constant time access.
     */
    private final HashMap<String, Object> attributeMap = new HashMap<>();

    /**
     * Adds an attribute to this object.
     * @param name name of attribute, which must be unique and not pre-existing in this object.
     * @param val an arbitrary value.
     * @throws IllegalArgumentException if added name is not unique.
     */
    public void addAttribute(@NotNull String name, Object val){
        if(attributeMap.containsKey(name))
            throw new IllegalArgumentException("Cannot add pre-existing attribute \"" + name +"\".");
        attributeList.add(new Attribute(name, val));
        attributeMap.put(name, val);
    }

    public void addAttributes(Attribute... attributes){
        for (Attribute attribute : attributes)
            addAttribute(attribute.name(), attribute.val());
    }

    /**
     * Removes an attribute identified by its name.
     * @param name name of the attribute.
     * @return the value of the attribute that were removed.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     */
    public Object removeAttribute(@NotNull String name){
        if(!attributeMap.containsKey(name))
            throw new AttributeNotFoundException(name);
        Object o = attributeMap.get(name);
        attributeMap.remove(name);
        for (int i = 0; i < attributeList.size(); i++) {
            if(attributeList.get(i).name().equals(name)){
                attributeList.remove(i);
                break;
            }
        }
        return o;
    }

    /**
     * @return whether there are no attributes stored in this object.
     */
    public boolean isEmpty(){
        return attributeMap.isEmpty();
    }

    /**
     * @return whether an object associates to a given name by an attribute.
     */
    public boolean contains(@NotNull String name){
        return attributeMap.containsKey(name);
    }

    /**
     * Retrieves a value associated by its name.
     * @param name name of the attribute.
     * @return the value of the attribute.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     */
    public @Nullable Object getItem(@NotNull String name){
        if(!attributeMap.containsKey(name))
            throw new AttributeNotFoundException(name);
        return attributeMap.get(name);
    }

    /**
     * Returns an object associated by a name with type {@link String}.
     * @param name name of the object.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     * @throws IllegalTypeException if the object associated with the name is non-null and
     *                                does not inherit the right type.
     */
    public @Nullable String getString(@NotNull String name){
        Object o = getItem(name);
        if(o == null) return null;
        if(!(o instanceof String s))
            throw new IllegalTypeException("Attribute identified by \"" + name + "\" is not of type String.");
        return s;
    }

    /**
     * Returns an object associated by a name with type {@link Boolean}.
     * @param name name of the object.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     * @throws IllegalTypeException if the object associated with the name is non-null and does not inherit the
     *                                right type.
     */
    public @Nullable Boolean getBool(@NotNull String name){
        Object o = getItem(name);
        if(o == null) return null;
        if(!(o instanceof Boolean b))
            throw new IllegalTypeException("Attribute identified by \"" + name + "\" is not of type Boolean.");
        return b;
    }

    /**
     * Returns an object associated by a name that inherits {@link JSONObject}.
     * @param name name of the object.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     * @throws IllegalTypeException if the object associated with the name is non-null and does not inherit
     *                                the right type.
     */
    public @Nullable JSONObject getJSONObject(@NotNull String name){
        Object o = getItem(name);
        if(o == null) return null;
        if(!(o instanceof JSONObject j))
            throw new IllegalTypeException("Attribute identified by \"" + name + "\" is not of type JSONObject.");
        return j;
    }

    /**
     * Returns an object associated by a name that inherits {@link Number}.
     * @param name name of the object.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     * @throws IllegalTypeException if the object associated with the name is non-null and does not inherit the
     *                                right type.
     */
    public @Nullable Number getNumber(@NotNull String name){
        Object o = getItem(name);
        if(o == null) return null;
        if(!(o instanceof Number n))
            throw new IllegalTypeException("Attribute identified by \"" + name + "\" is not of type Number.");
        return n;
    }

    /**
     * Returns an object associated by a name that has the form of an array, (can be safely cast to an object array).
     * @param name name of the object.
     * @throws AttributeNotFoundException if no value associated with the given name is found.
     * @throws IllegalTypeException if the object associated with the name is non-null and is not an array.
     */
    public @Nullable Object[] getArray(@NotNull String name){
        Object o = getItem(name);
        if(o == null) return null;
        if(!(o.getClass().isArray()))
            throw new IllegalTypeException("Attribute identified by \"" + name + "\" is not an Array.");
        return (Object[]) o;
    }

    /**
     * Used for serialisation in {@link JSONSerialisable}.
     * @return a copy of the list of attributes in correct order.
     */
    @Override
    public List<Attribute> jsonAttributes() {
        return new ArrayList<>(attributeList);
    }

    /**
     * String representation defined as its JSON-string representation.
     */
    @Override
    public String toString() {
        return serialise();
    }

    /**
     * Two JSON objects are equal if the set of one set of attributes is equal to the other set of attributes (see {@link Attribute#equals(Object)})
     * <b>and</b> that the values associated by each name are equal. Note that in the case of equivalence, order does not
     * matter, but unlike attribute equivalence, the equivalence value-wise does matter. Note that this definition is
     * recursive in nature. Note also for arrays, order does matter, since an array on its own is considered an object,
     * and ordering is considered in array equivalence.
     */
    @Override
    public boolean equals(Object obj) {

        //Trivial conditions
        if(obj == null) return false;
        if(super.equals(obj)) return true;
        if(!(obj instanceof JSONObject jsonObject))
            return false;

        //Checks if the set of all attribute names are equal.
        Set<String> set1 = jsonObject.attributeMap.keySet();
        Set<String> set2 = this.attributeMap.keySet();
        if(!set1.equals(set2)) return false;

        //Checks equivalence between values.
        for (Attribute a : attributeList){
            Object o1 = this.getItem(a.name());
            Object o2 = jsonObject.getItem(a.name());

            if(o1 != null && o2 == null)
                return false;
            if(o1 == null && o2 != null)
                return false;
            if(o1 != null){ //Both not null
                if(o1.getClass().isArray() && o2.getClass().isArray()){
                    Object[] arr1 = (Object[]) o1;
                    Object[] arr2 = (Object[]) o2;
                    if(arr1.length != arr2.length)
                        return false;
                    for (int i = 0; i < arr1.length; i++) {
                        if(!arr1[i].equals(arr2[i]))
                            return false;
                    }
                }
                else if(!o1.equals(o2))
                    return false;
            }
        }
        return true;
    }
}
