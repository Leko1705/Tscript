package com.tscript.runtime.stroage;

import com.tscript.runtime.typing.TObject;

public interface Pool {

    String loadName(byte b1, byte b2);

    TObject loadConstant(byte b1, byte b2);

}
