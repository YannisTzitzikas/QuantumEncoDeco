package com.csd.core.model;

import java.util.List;

import com.csd.core.model.msg.DataMsg;
import com.csd.core.model.msg.EosMsg;
import com.csd.core.model.msg.Msg;

import java.util.Collections;

interface Stage<I,O> {
    String name();
    default void onOpen(PipelineContext ctx) throws Exception {}
    // Return zero or more outputs (DATA/EOS); Router handles distribution
    List<Msg> onData(DataMsg<I> in, PipelineContext ctx) throws Exception;
    default List<Msg> onEos(PipelineContext ctx) throws Exception { return Collections.singletonList(EosMsg.INSTANCE); }
    default void onClose(PipelineContext ctx) throws Exception {}
}