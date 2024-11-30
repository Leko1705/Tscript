package com.tscript.runtime.stroage.loading;

public interface LoadingConstants {

    int MAGIC_NUMBER = 0xDEAD;

    int
            POOL_TYPE_INT = 0,
            POOL_TYPE_REAL = 1,
            POOL_TYPE_STRING = 2,
            POOL_TYPE_BOOL = 3,
            POOL_TYPE_NULL = 4,
            POOL_TYPE_RANGE = 5,
            POOL_TYPE_ARRAY = 6,
            POOL_TYPE_DICTIONARY = 7,
            POOL_TYPE_UTF8 = 11;


}
