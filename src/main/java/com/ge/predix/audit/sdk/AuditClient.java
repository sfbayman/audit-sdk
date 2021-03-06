package com.ge.predix.audit.sdk;

import com.ge.predix.audit.sdk.config.AuditConfiguration;
import com.ge.predix.audit.sdk.exception.AuditException;
import com.ge.predix.audit.sdk.message.AuditEvent;
import com.ge.predix.audit.sdk.util.CustomLogger;
import com.ge.predix.audit.sdk.util.LoggerUtils;
import com.ge.predix.eventhub.EventHubClientException;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

/**
 * Created by Martin Saad on 2/12/2017.
 */
public class AuditClient<T extends AuditEvent> {
    private static CustomLogger log = LoggerUtils.getLogger(AuditClient.class.getName());

    private final CommonClientInterface<T> auditClientAsyncImpl;
    @Getter(AccessLevel.PACKAGE)
    private final AuditConfiguration auditConfiguration;

    /**
     * Returns an Async audit client to publish audit messages.
     * @param configuration - auditConfiguration
     * @param callback - callback to be invoked for client's changes.
     * @throws AuditException - when the configuration is invalid.
     * @throws EventHubClientException - when fail to build eventhub client.
     */
    public AuditClient(AuditConfiguration configuration, AuditCallback<T> callback)
            throws AuditException, EventHubClientException {
        LoggerUtils.setLogLevelFromVcap();
        ConfigurationValidator configurationValidator = ConfigurationValidatorFactory.getConfigurationValidator();
        configurationValidator.validateAuditConfiguration(configuration);
        this.auditConfiguration = configuration;
        DirectMemoryMonitor directMemoryMonitor = DirectMemoryMonitor.getInstance();
        //because only prints in debug
        if(LoggerUtils.isDebugLogLevel()) {
            directMemoryMonitor.startMeasuringDirectMemory();
        }
        TracingHandler tracingHandler = TracingHandlerFactory.newTracingHandler(auditConfiguration, "ASYNC");
        auditClientAsyncImpl = new AuditClientAsyncImpl<>(auditConfiguration, callback, tracingHandler );
    }

    /**
     * Logs an audit event asynchronously.
     * Result of this operation will be propagated through the AuditCallback.
     *
     * @param event - the event to log.
     * @throws AuditException - if an unexpected error occurred with auditing.
     *         IllegalStateException - if the client was shutdown.
     */
    public void audit(T event) throws AuditException{
        auditClientAsyncImpl.audit(event);
    }

    /**
     * Logs audit events asynchronously.
     * Result of this operation will be propagated through the AuditCallback.
     * @param events - the events to log
     * @throws AuditException - if an unexpected error occurred with auditing.
     *         IllegalStateException - if the client was shutdown.
     */
    public void audit(List<T> events) throws AuditException {
        auditClientAsyncImpl.audit(events);
    }

    /**
     * Reconnects the audit client.
     * @throws EventHubClientException - if the attempt fails.
     *         IllegalStateException - if the client was shutdown.
     */
    public void reconnect() throws EventHubClientException {
        auditClientAsyncImpl.reconnect();
    }

    /**
     * Sends a tracing message to audit service.
     * @throws EventHubClientException
     *         IllegalStateException - if the client was shutdown, or tracing is not enabled.
     */
    public void trace() throws EventHubClientException {
        if(auditConfiguration.isTraceEnabled()) {
            auditClientAsyncImpl.trace();
        }
        else{
            throw new IllegalStateException("Trace is not enabled.");
        }
    }

    /**
     * Shuts-down this client.
     * this client cannot be restarted after it was shutdown
     * @throws EventHubClientException - in case there was an error closing resources
     */
    public void shutdown() throws EventHubClientException {
        auditClientAsyncImpl.shutdown();
        DirectMemoryMonitor.getInstance().shutdown();
    }

    /**
     * Returns the state of this client.
     *
     * @return state of Audit Client
     */
    public AuditClientState getState(){
        return auditClientAsyncImpl.getAuditClientState();
    }

    /**
     * Sets a new authentication token for this client.
     * This API can only be invoked if the client was created with an Authentication token.
     * @param authToken - the new token
     * @throws EventHubClientException - if failed to set the token.
     * @throws AuditException - if the operation is not supported with this client configuration. i.e- the client was not
     * created with an authToken configuration.
     *         IllegalStateException - if the client was shutdown.
     */
    public void setAuthToken(String authToken) throws EventHubClientException, AuditException {
        if(auditConfiguration.getAuthenticationMethod() == AuthenticationMethod.AUTH_TOKEN) {
            auditClientAsyncImpl.setAuthToken(authToken);
            log.warning("new auth token was successfully set");
        }
        else{
            throw new AuditException("setAuthToken operation is not supported for this client configuration");
        }

    }

}
