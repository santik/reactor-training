package com.tnt.service;

import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {

    public static Retry retrySpec() {
        return Retry.fixedDelay(10, Duration.ofMillis(500))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure()))
                ;
    }

}
