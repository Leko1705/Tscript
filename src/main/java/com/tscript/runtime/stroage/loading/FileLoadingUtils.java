package com.tscript.runtime.stroage.loading;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringJoiner;

public class FileLoadingUtils {

    public static String getExtensionString(String[] extensions) {
        StringJoiner joiner = new StringJoiner(File.separator, File.separator, "");
        for (String ex : extensions)
            joiner.add(ex);
        return joiner.toString();
    }

    public static boolean checkIfBytecodeFile(File file, String[] expected) {
        if (!file.exists()) return false;

        try(FileReader reader = new FileReader(file)) {
            if (reader.read() != 0xD) return false;
            if (reader.read() != 0xE) return false;
            if (reader.read() != 0xA) return false;
            if (reader.read() != 0xD) return false;

            StringBuilder sb = new StringBuilder();
            int b;
            while ((b = reader.read()) != '\0')
                sb.append((char) b);

            String[] got = sb.toString().split("[.]");

            if (got.length != expected.length) return false;

            for (int i = 0; i < expected.length; i++)
                if (!expected[i].equals(got[i]))
                    return false;

            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

}
