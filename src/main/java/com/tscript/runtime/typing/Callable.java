package com.tscript.runtime.typing;

import com.tscript.runtime.core.ExecutionException;
import com.tscript.runtime.core.InternalRuntimeErrorMessages;
import com.tscript.runtime.core.TThread;
import com.tscript.runtime.utils.FastMergedList;


import java.util.*;

public interface Callable extends TObject {

    
    String getName();

    
    Parameters getParameters(TThread thread);


    boolean isVirtual();

    
    default TObject call(TThread thread, List<TObject> arguments) {
        Parameters parameters = getParameters(thread);

        if (arguments.isEmpty() && parameters.count() == 0) {
            return eval(thread, List.of());
        }

        if (arguments.size() > parameters.count()) {
            thread.reportRuntimeError(InternalRuntimeErrorMessages.tooManyParameters(this));
            return null;
        }

        int lastNonDefined = parameters.getLastNonDefinedParameterIndex();

        if (arguments.size() <= lastNonDefined){
            List<String> names = parameters.getNames();
            String missing = names.get(lastNonDefined);
            thread.reportRuntimeError(InternalRuntimeErrorMessages.missingParameter(missing));
            return null;
        }

        List<TObject> missingDefaults = parameters.getTail(arguments.size());
        return eval(thread, new FastMergedList<>(arguments, missingDefaults));
    }


    default TObject call(TThread thread, List<String> names, List<TObject> arguments) {
        if (names.size() != arguments.size()) {
            throw new ExecutionException("names.size() != arguments.size()");
        }

        Parameters parameters = getParameters(thread);

        if (names.size() > parameters.count()){
            thread.reportRuntimeError(InternalRuntimeErrorMessages.tooManyParameters(this));
            return null;
        }

        // flatten is an ArrayList with parameters.count() elements by default
        List<TObject> flatten = Arrays.asList(new TObject[parameters.count()]);

        int index = 0;
        Iterator<TObject> valueItr = arguments.iterator();
        for (String givenName : names){

            if (!parameters.hasName(givenName)){
                thread.reportRuntimeError(InternalRuntimeErrorMessages.hasNoParameters(this, givenName));
                return null;
            }

            TObject givenValue = valueItr.next();
            if (givenValue == null) {
                givenValue = parameters.getDefaultValue(givenName);
                if (givenValue == null) {
                    thread.reportRuntimeError(InternalRuntimeErrorMessages.missingParameter(givenName));
                    return null;
                }
            }

            if (flatten.get(index) != null){
                thread.reportRuntimeError(InternalRuntimeErrorMessages.parameterAlreadyAssigned(givenName));
                return null;
            }

            flatten.set(index, givenValue);
        }

        return eval(thread, flatten);
    }

    
    TObject eval(TThread thread, List<TObject> params);

}
