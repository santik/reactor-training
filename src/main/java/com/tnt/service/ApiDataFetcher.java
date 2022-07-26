package com.tnt.service;

import com.tnt.model.ApisProperties.ApiProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface ApiDataFetcher<T> {
    void initializeClient(ApiProperties properties);
    Mono<Map<String, Optional<T>>> fetch(List<String> ids);
}
