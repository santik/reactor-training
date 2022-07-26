package com.tnt.service;

import com.tnt.model.AggregatedResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ApiAggregationService {

    private final PricingService pricingService;
    private final TrackService trackService;
    private final ShipmentService shipmentService;

    public Mono<AggregatedResult> fetch(
        List<String> pricingOptional,
        List<String> trackOptional,
        List<String> shipmentsOptional) {

        Mono<Map<String, Double>> pricingResult = pricingService.fetchPrices(pricingOptional);
        Mono<Map<String, String>> trackResult = trackService.fetchTracks(trackOptional);
        Mono<Map<String, List<String>>> shipmentResult = shipmentService.fetchShipments(shipmentsOptional);

        return Mono.zip(pricingResult, trackResult, shipmentResult).map(
            tuple -> new AggregatedResult(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }
}
