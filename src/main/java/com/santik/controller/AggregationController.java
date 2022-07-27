package com.santik.controller;

import com.santik.model.AggregatedResult;
import com.santik.service.ApiAggregationService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/aggregation")
@RequiredArgsConstructor
public class AggregationController {

    private final ApiAggregationService apiAggregator;

    @GetMapping
    Mono<AggregatedResult> get(
        @RequestParam Optional<List<String>> pricing,
        @RequestParam Optional<List<String>> track,
        @RequestParam Optional<List<String>> shipments) {

        return apiAggregator.fetch(
                pricing.orElse(Collections.emptyList()),
                track.orElse(Collections.emptyList()),
                shipments.orElse(Collections.emptyList())
        );
    }
}
