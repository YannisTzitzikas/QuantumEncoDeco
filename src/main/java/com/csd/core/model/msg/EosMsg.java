package com.csd.core.model.msg;

public final class EosMsg implements Msg {
    public static final EosMsg INSTANCE = new EosMsg();
    private EosMsg(){}
    public boolean isEos(){ return true; }
}