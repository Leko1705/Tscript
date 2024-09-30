package com.tscript.runtime.typing;

import java.util.*;

public class Parameters {

    public static Parameters newInstance(){
        return new Parameters();
    }


    private final List<String> orderedParameterNames = new ArrayList<>();
    private final List<TObject> orderedDefaultList = new ArrayList<>();

    private final Map<String, TObject> defaultValues = new LinkedHashMap<>();

    private int lastNonDefinedParameter = 0;

    private Parameters() {
    }

    public Parameters add(String name, TObject defaultValue) {
        if (defaultValue == null)
            lastNonDefinedParameter = defaultValues.size();
        defaultValues.put(name, defaultValue);
        orderedParameterNames.add(name);
        orderedDefaultList.add(defaultValue);
        return this;
    }

    public int count(){
        return defaultValues.size();
    }

    public List<String> getNames() {
        return orderedParameterNames;
    }

    public boolean hasName(String name){
        return orderedParameterNames.contains(name);
    }

    public TObject getDefaultValue(String name) {
        return defaultValues.get(name);
    }

    protected int getLastNonDefinedParameterIndex() {
        return lastNonDefinedParameter;
    }

    protected List<TObject> getTail(int fromIndex){
        return orderedDefaultList.subList(fromIndex, orderedDefaultList.size());
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "defaultValues=" + defaultValues +
                '}';
    }
}
