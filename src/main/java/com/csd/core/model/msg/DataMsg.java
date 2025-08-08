package com.csd.core.model.msg;

public final class DataMsg<T> implements Msg {
    final T payload;
    DataMsg(T p){ this.payload = p; }
    public boolean isEos(){ return false; }
}