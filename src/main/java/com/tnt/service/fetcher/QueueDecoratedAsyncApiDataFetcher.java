package com.tnt.service.fetcher;

import com.tnt.service.ApiDataFetcher;
import com.tnt.model.ApisProperties.ApiProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Scope("prototype")
class QueueDecoratedAsyncApiDataFetcher<T> implements ApiDataFetcher<T> {

    private static final int SCHEDULER_INITIAL_DELAY = 100;
    private static final int SCHEDULER_FIXED_DELAY = 100;
    private static final int MAX_SIZE = 5;
    private static final int MAX_DELAY = 5;

    private Flux<Map<String, Optional<T>>> fetcher;
    private FluxSink<Map<String, Optional<T>>> sink;
    private ConcurrentLinkedQueue<List<String>> queue;
    private LocalDateTime lastFetch;
    private AsyncApiDataFetcher<T> asyncApiDataFetcher;

    @PostConstruct
    public void setUp() {
        queue = new ConcurrentLinkedQueue<>();
        lastFetch = LocalDateTime.now();
        fetcher = Flux.push(this::populateFluxFromApi).cache();
    }

    public void initializeClient(ApiProperties apiProperties) {
        asyncApiDataFetcher = new AsyncApiDataFetcher<>(apiProperties);
    }

    @Scheduled(fixedDelay = SCHEDULER_FIXED_DELAY, initialDelay = SCHEDULER_INITIAL_DELAY)
    public void populateFromTheQueue() {
        populateFluxFromApi(sink);
    }

    public Mono<Map<String, Optional<T>>> fetch(List<String> ids) {
        Mono<Map<String, Optional<T>>> mapMono = getMapMono(ids);
        queue.add(ids);
        return mapMono;
    }

    private void populateFluxFromApi(FluxSink<Map<String, Optional<T>>> sink) {

        if (Objects.isNull(sink)) {
            return;
        }

        this.sink = sink;

        if (queue.size() < MAX_SIZE && !needToFetchByTimeout()) {
            sink.next(new HashMap<>());
            return;
        }
        Set<String> allIds = new HashSet<>();

        //we always do MAX_SIZE even if initially it wasn't enough because it can be added any time
        IntStream.range(0, MAX_SIZE).forEach(i -> {
            List<String> poll = queue.poll();
            if (Objects.nonNull(poll)) {
                allIds.addAll(poll);
            }
        });

        lastFetch = LocalDateTime.now();

        if (!allIds.isEmpty()) {
            asyncApiDataFetcher.fetch(new ArrayList<>(allIds)).subscribe(sink::next);
        } else {
            sink.next(new HashMap<>());
        }
    }

    private boolean needToFetchByTimeout() {
        return Duration.between(lastFetch, LocalDateTime.now()).getSeconds() > MAX_DELAY;
    }

    private Mono<Map<String, Optional<T>>> getMapMono(List<String> ids) {

        return Mono.create(emitter -> {
            //we need it to collect items for particular request
            Map<String, Optional<T>> response = new HashMap<>();

            Flux.from(fetcher)
                .map(
                    result -> result.entrySet().stream()
                        .filter(entry -> ids.contains(entry.getKey())) //we don't care about items which are not in a request
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
                .filter(result -> !result.isEmpty()).subscribe(item -> {
                    response.putAll(item);
                    if (response.keySet().containsAll(ids)) { //checking if all the data is collected
                        emitter.success(response);
                    }
                });
        });
    }
}
