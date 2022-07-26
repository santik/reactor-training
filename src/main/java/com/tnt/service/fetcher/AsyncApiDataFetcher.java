package com.tnt.service.fetcher;

import com.tnt.model.ApisProperties.ApiProperties;
import com.tnt.service.ApiDataFetcher;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class AsyncApiDataFetcher<T> implements ApiDataFetcher<T> {

    private static final int API_TIMEOUT_MILLIS = 2000;
    private ApiProperties apiProperties;
    private WebClient client;

    public AsyncApiDataFetcher(ApiProperties apiProperties) {
        initializeClient(apiProperties);
    }

    @Override
    public void initializeClient(ApiProperties properties) {
        apiProperties = properties;
        client = WebClient.create(apiProperties.getBaseUrl());
    }

    public Mono<Map<String, Optional<T>>> fetch(List<String> ids) {

        if (Objects.isNull(client)) {
            throw new RuntimeException("Client is not initialized");
        }

        return client
            .get()
            .uri(apiProperties.getUri(), String.join(",", ids))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Optional<T>>>() {})
            .timeout(Duration.ofMillis(API_TIMEOUT_MILLIS))
            .doOnError(throwable -> log.error("Error during API call: {} {}", apiProperties.toString(), throwable))
            .onErrorReturn(ids.stream().collect(Collectors.toMap(Function.identity(), id -> Optional.empty()))); //sending empty to avoid complexity when collecting results from the queue
    }

}
