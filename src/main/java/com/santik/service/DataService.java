package com.santik.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class DataService<T> {

    public static final int MAX_BUFFER_SIZE = 5;
    public static final Duration MAX_BUFFER_TIME = Duration.ofSeconds(5);
    private final WebClient webClient;
    private final String apiUri;

    private final Sinks.Many<List<String>> reqSink = Sinks.many().replay().all();
    private final Sinks.Many<Map<String, T>> resSink = Sinks.many().replay().all();

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
                            .subscribe(map -> {
                                log.info("Received {}", map);
                                resSink.emitNext(map, Sinks.EmitFailureHandler.FAIL_FAST);
                            });
                });

    }

    public Mono<Map<String, T>> fetch(List<String> list) {
        Mono<Map<String, T>> mapMono = getRes(list);
        reqSink.emitNext(list, Sinks.EmitFailureHandler.FAIL_FAST);
        return mapMono;
    }

    private Mono<Map<String, T>> getPricesFromClient(List<String> list) {

        return webClient
                .get()
                .uri(apiUri, String.join(",", list))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, T>>() {
                })
                .onErrorReturn(
                        e -> e instanceof WebClientRequestException || e instanceof WebClientResponseException || e instanceof TimeoutException,
                        list.stream().collect(Collectors.toMap(Function.identity(), getDefaultObjectFunction()))
                );
    }

    public abstract Function<Object, T> getDefaultObjectFunction();

    private Mono<Map<String, T>> getRes(List<String> list) {

        Map<String, T> initialResult = new HashMap<>();

        return resSink.asFlux()
                .scan(initialResult, (accu, next) -> {
                    for (String req : list) {
                        if (next.containsKey(req)) {
                            accu.put(req, next.get(req));
                        }
                    }
                    return accu;
                })
                .takeUntil(map -> map.keySet().containsAll(list))
                .last();

    }
}
