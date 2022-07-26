package com.tnt.service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class TestSinkService {

    public static final int MAX_BUFFER_SIZE = 2;
    public static final Duration MAX_BUFFER_TIME = Duration.ofMillis(2000);

    private Sinks.Many<List<String>> reqSink = Sinks.many().replay().all();
    private Sinks.Many<Map<String, Double>> resSink = Sinks.many().replay().all();

    public void startListen() {
        reqSink
                .asFlux()
                .bufferTimeout(MAX_BUFFER_SIZE, MAX_BUFFER_TIME)
                .subscribe(lists -> {
                    List<String> collect = lists.stream()
                            .flatMap(List::stream)
                            .distinct()
                            .collect(Collectors.toList());

                    log.info("Fetching from client {}", collect);

                    getDataFromClientDumb(collect)
                            .subscribe(stringDoubleMap -> {
                                log.info("Received from client {}", stringDoubleMap);
                                resSink.emitNext(stringDoubleMap, Sinks.EmitFailureHandler.FAIL_FAST);
                            });
                });

    }

    public Mono<Map<String, Double>> fetchData(List<String> list) {
        Mono<Map<String, Double>> mapMono = getRes(list);
        reqSink.emitNext(list, Sinks.EmitFailureHandler.FAIL_FAST);
        return mapMono;
    }


    private Mono<Map<String, Double>> getDataFromClientDumb(List<String> list) {
        Map<String, Double> data = list.stream().collect(Collectors.toMap(Function.identity(), v -> Math.random()));
        return Mono.just(data);
    }


    private Mono<Map<String, Double>> getRes(List<String> list) {

        Flux<Map<String, Double>> mapFlux = resSink.asFlux();
        Map<String, Double> initial = new HashMap<>();
        return mapFlux
                .scan(initial, (accu, next) -> {
                    for (String req : list) {
                        if (next.containsKey(req)) {
                            accu.put(req, next.get(req));
                        }
                    }
                    return accu;
                })
                .takeUntil(stringDoubleMap -> stringDoubleMap.keySet().containsAll(list))
                .last();
    }
}
