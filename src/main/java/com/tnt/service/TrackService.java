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
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackService {

    private final WebClient trackClient;
    private final ApisProperties aggregatedApiProperties;

    private Sinks.Many<List<String>> reqSink = Sinks.many().unicast().onBackpressureBuffer();
    private Sinks.Many<Map<String, String>> resSink = Sinks.many().multicast().onBackpressureBuffer();

    @PostConstruct
    public void startListen() {
        reqSink
                .asFlux()
                .bufferTimeout(5, Duration.ofSeconds(5))
                .subscribe(lists -> {
                    List<String> collect = lists.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());

                    getTracksFromClient(collect)
                            .subscribe(stringDoubleMap -> resSink.tryEmitNext(stringDoubleMap));
                });

    }


    public Mono<Map<String, String>> fetchTracks(List<String> list) {
        reqSink.tryEmitNext(list);
        return getMapMono(list);
    }

    private Mono<Map<String, String>> getTracksFromClient(List<String> list) {
        return trackClient
                .get()
                .uri(aggregatedApiProperties.getTrack().getUri(), String.join(",", list))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .onErrorReturn(e -> e instanceof WebClientResponseException || e instanceof TimeoutException, list.stream().collect(Collectors.toMap(Function.identity(), v -> "")))
//                .retryWhen(RetryUtil.retrySpec())
                ;
    }

    private Mono<Map<String, String>> getMapMono(List<String> ids) {

        return Mono.create(emitter -> {
            //we need it to collect items for particular request
            Map<String, String> response = new HashMap<>();

            resSink.asFlux()
                    .map(
                            result -> result.entrySet().stream()
                                    .filter(entry -> ids.contains(entry.getKey())) //we don't care about items which are not in a request
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .filter(result -> !result.isEmpty()).subscribe(item -> {
                        response.putAll(item);
                        if (response.keySet().containsAll(ids)) { //checking if all the data is collected
                            emitter.success(response);
                        }
                    });
        });
    }
}
