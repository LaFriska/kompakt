package com.friska;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class JSONObject implements JSONSerialisable{

    private final List<Attribute> attributeList = new ArrayList<>();

    private final HashMap<String, Object> attributeMap = new HashMap<>();

    public void addAttribute(@NotNull String name, Object val){
        if(attributeMap.containsKey(name))
            throw new IllegalArgumentException("Cannot add pre-existing attribute \"" + name +"\".");
        attributeList.add(new Attribute(name, val));
        attributeMap.put(name, val);
    }

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

    public @Nullable Object getItem(@NotNull String name){
        if(!attributeMap.containsKey(name)) throw new AttributeNotFoundException(name);
        return attributeMap.get(name);
    }

    @Override
    public List<Attribute> jsonAttributes() {
        return attributeList;
    }
}
