package com.tnt.service;

import com.tnt.model.ApisProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    public static final int MAX_BUFFER_SIZE = 5;
    public static final Duration MAX_BUFFER_TIME = Duration.ofMillis(500);
    private final WebClient pricingClient;
    private final ApisProperties aggregatedApiProperties;

    private Sinks.Many<List<String>> reqSink = Sinks.many().replay().all();
    private Sinks.Many<Map<String, Double>> resSink = Sinks.many().replay().all();

    @PostConstruct
    public void startListen() {
        reqSink
                .asFlux()
                .bufferTimeout(MAX_BUFFER_SIZE, MAX_BUFFER_TIME)
                .subscribe(lists -> {
                    List<String> collect = lists.stream()
                            .flatMap(List::stream)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("Fetching {}", collect);

                    getPricesFromClient(collect)
                            .subscribe(stringDoubleMap -> {
                                log.info("Received {}", stringDoubleMap);
                                resSink.emitNext(stringDoubleMap, Sinks.EmitFailureHandler.FAIL_FAST);
                            });
                });

    }

    public Mono<Map<String, Double>> fetchPrices(List<String> list) {
        Mono<Map<String, Double>> mapMono = getRes(list);
        reqSink.emitNext(list, Sinks.EmitFailureHandler.FAIL_FAST);
        return mapMono;
    }

    private Mono<Map<String, Double>> getPricesFromClient(List<String> list) {

        return pricingClient
                .get()
                .uri(aggregatedApiProperties.getPricing().getUri(), String.join(",", list))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Double>>() {
                })
                .onErrorReturn(e -> e instanceof WebClientResponseException || e instanceof TimeoutException, list.stream().collect(Collectors.toMap(Function.identity(), v -> Double.valueOf(0))))
//                .retryWhen(RetryUtil.retrySpec())
                ;
    }


    private Mono<Map<String, Double>> getRes(List<String> list) {

        Map<String, Double> initialResult = new HashMap<>();

        return resSink.asFlux()
                .scan(initialResult, (accu, next) -> {
                    log.info("Next {}", next);
                    for (String req : list) {
                        if (next.containsKey(req)) {
                            accu.put(req, next.get(req));
                        }
                    }
                    log.info("Accu {}", next);
                    return accu;
                })
                .takeUntil(stringDoubleMap -> stringDoubleMap.keySet().containsAll(list))
                .last();

    }
}
