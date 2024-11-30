package com.tscript.compiler.impl.utils;

import com.tscript.compiler.source.utils.ClassNameFormatter;

import java.util.List;
import java.util.StringJoiner;

public class DottetClassNameFormatter implements ClassNameFormatter {

    @Override
    public String format(List<String> fullName) {
        StringJoiner joiner = new StringJoiner(".");
        for (String name : fullName) {
            joiner.add(name);
        }
        return joiner.toString();
    }
}
