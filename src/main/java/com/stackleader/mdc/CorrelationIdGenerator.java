package com.stackleader.mdc;

import com.google.common.base.Stopwatch;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class CorrelationIdGenerator implements HttpHandler {

    private final HttpHandler handler;
    private static final Logger LOG = LoggerFactory.getLogger(CorrelationIdGenerator.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public CorrelationIdGenerator(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String correlationId = Optional.ofNullable(exchange.getRequestHeaders().get(CORRELATION_ID_HEADER))
                .map(HeaderValues::getFirst)
                .orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", correlationId);
        if (LOG.isTraceEnabled()) {
            exchange.getRequestHeaders().forEach(header
                    -> header.forEach(value -> LOG.trace("header={} value={}", header.getHeaderName(), value))
            );
        }
        LOG.info("[METRICS] Starting timer for requestMethod={} requestPath={} request", exchange.getRequestMethod(), exchange.getRequestPath());
        exchange.addExchangeCompleteListener(new ResponseTimeLogger());
        wrapExecutorToPropogateMdcContext(exchange);
        handler.handleRequest(exchange);
    }

    private void wrapExecutorToPropogateMdcContext(HttpServerExchange exchange) {
        Executor dispatchExecutor = exchange.getDispatchExecutor();
        if (dispatchExecutor == null) {
            dispatchExecutor = exchange.getConnection().getWorker();
        }
        exchange.setDispatchExecutor(new WrappingMdcExecutor(dispatchExecutor));
    }

    private class ResponseTimeLogger implements ExchangeCompletionListener {

        private Stopwatch stopwatch;

        public ResponseTimeLogger() {
            stopwatch = Stopwatch.createStarted();
        }

        @Override
        public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
            try {
                LOG.info("[METRICS] Response Time for requestMethod={} requestPath={}: {} ms, httpResponseCode={}", exchange.getRequestMethod(), exchange.getRequestPath(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS), exchange.getStatusCode());

            } finally {
                if (nextListener != null) {
                    nextListener.proceed();
                }
            }
        }
    }

}
