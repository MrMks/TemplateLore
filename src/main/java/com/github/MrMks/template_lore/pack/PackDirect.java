package com.github.MrMks.template_lore.pack;

public class PackDirect<T> implements Pack<T> {

    private final T tmp;
    public PackDirect(T tmp){
        this.tmp = tmp;
    }

    @Override
    public T toR() {
        return tmp;
    }
}
