package com.csd.runtime;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.csd.core.PipelineContext;
import com.csd.core.model.graph.Node;
import com.csd.core.model.graph.Stage;
import com.csd.core.model.msg.DataMsg;
import com.csd.core.model.msg.EosMsg;
import com.csd.core.model.msg.Msg;

final class Runner implements Runnable {
    private final Node node;
    private final Router router;
    private final PipelineContext ctx;
    private final AtomicBoolean done = new AtomicBoolean(false);

    Runner(Node node, Router router, PipelineContext ctx){
        this.node = node; this.router = router; this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            node.stage.onOpen(ctx);
            while (!done.get()) {
                Msg msg = node.inbox.take();
                if (msg.isEos()) {
                    List<Msg> outs = node.stage.onEos(ctx);
                    router.emit(node.name, outs);
                    done.set(true);
                } else {
                    List<Msg> outs = ((Stage<Object,Object>)node.stage).onData((DataMsg<Object>) msg, ctx);
                    if (outs != null && !outs.isEmpty()) {
                        router.emit(node.name, outs);
                    }
                }
            }
            node.stage.onClose(ctx);
        } catch (Throwable t){
            t.printStackTrace();
            // Best-effort propagate EOS to unblock downstreams
            router.emit(node.name, Collections.singletonList(EosMsg.INSTANCE));
        }
    }
}