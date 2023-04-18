package com.rhc.mdc;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class WrappingMdcExecutor implements Executor {

    private static final Logger LOG = LoggerFactory.getLogger(WrappingMdcExecutor.class);
    private final Executor delegate;

    public WrappingMdcExecutor(Executor executor) {
        this.delegate = executor;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrapTask(command));
    }

    private <T> Callable<T> wrapTask(Callable<T> callable) {
        final Map<String, String> currentContext = MDC.getCopyOfContextMap();
        LOG.debug("Wrapping callable to inject current MDC context");
        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            if (currentContext == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(currentContext);
                LOG.debug("MDC context injected");
            }
            try {
                return callable.call();
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }

    private Runnable wrapTask(Runnable command) {
        final Callable<Object> wrapped = wrapTask(Executors.callable(command, null));
        return () -> {
            try {
                wrapped.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
