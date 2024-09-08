package com.tscript.tscriptc.generation.writers;

import com.tscript.tscriptc.generation.compiled.pool.*;

public interface PoolEntryWriter {

    void writeInteger(IntegerEntry entry);

    void writeFloat(FloatEntry entry);

    void writeString(StringEntry entry);

    void writeNull(NullEntry entry);

    void writeBoolean(BooleanEntry entry);

    void writeRange(RangeEntry entry);

    void writeArray(ArrayEntry entry);

    void writeDictionary(DictionaryEntry entry);

    void writeUTF8(UTF8Entry entry);

}
