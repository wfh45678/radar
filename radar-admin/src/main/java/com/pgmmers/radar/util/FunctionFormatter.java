package com.pgmmers.radar.util;

/**
 * @author xushuai
 */
@FunctionalInterface
public interface FunctionFormatter<TValue,TRow,TIndex,R> {
    R apply(TValue value, TRow row, TIndex index);
}
