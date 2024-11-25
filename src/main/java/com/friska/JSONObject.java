package com.friska;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONObject {

    private List<Object> objects;

    private Set<String> allFields;

    private Map<String, String> stringFields;
    private Map<String, Number> numberFields;

    private Map<String, Boolean> booleanFields;

    private Map<String, Object[]> arrayFields;

    private Set<String> nulls;

}
