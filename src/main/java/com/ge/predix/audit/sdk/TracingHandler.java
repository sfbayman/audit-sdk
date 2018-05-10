package com.ge.predix.audit.sdk;

import com.ge.predix.audit.sdk.message.AuditEvent;
import com.ge.predix.audit.sdk.message.AuditTracingEvent;
import com.ge.predix.audit.sdk.message.tracing.Checkpoint;
import com.ge.predix.audit.sdk.message.tracing.LifeCycleEnum;
import com.ge.predix.eventhub.Ack;

import java.util.Optional;

/**
 * Created by 212582776 on 2/21/2018.
 */
public interface TracingHandler {

    /**
     * @returns AuditTracingEvent optional.
     * it will return the sent message or null if no message was sent
     */
    Optional<AuditTracingEvent> sendInitialCheckpoint();

    void sendCheckpoint(Ack ack);

    void sendCheckpoint(AuditEvent event, LifeCycleEnum lifeCycleStatus, String message);

    boolean isTracingAck(Ack ack);

    boolean isTracingEvent(AuditEvent event);
}