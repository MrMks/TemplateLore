package com.github.mrmks.mc.template;

public interface ITokenProvider {
    boolean has(String tk);
    String parse(String tk, String val);
    String parse(String val);
}
