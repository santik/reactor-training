package com.tnt.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class PricingServiceTest {

    @Autowired
    PricingService pricingService;

    @Test
    void fetchPrices() {

        int numberOfRequests = 5;
        List<List<String>> requests = new ArrayList<>();
        List<Mono<Map<String, Double>>> monos = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i ++ ) {
            List<String> pricing = generateRequestParams().getPricing();
            requests.add(pricing);
            Mono<Map<String, Double>> prices = pricingService.fetchPrices(pricing);
//            prices.subscribe(stringDoubleMap -> {
//                log.info("Prices {}", stringDoubleMap);
//            });
            StepVerifier.create(prices).assertNext(stringDoubleMap -> {
                        log.info("Received in response {}", stringDoubleMap);
                        assertTrue(stringDoubleMap.keySet().containsAll(pricing));
                    }).verifyComplete();
//            monos.add(prices);
        }

//        for (int i = 0;  i < numberOfRequests; i ++ ) {
//            int finalI = i;
//            StepVerifier.create(monos.get(i))
//                    .assertNext(stringDoubleMap -> {
//                        log.info("Received {} {}", finalI, stringDoubleMap);
//                        assertTrue(stringDoubleMap.keySet().containsAll(requests.get(finalI)));
//                    })
//                    .verifyComplete();
//
//        }
    }


    private RequestParams generateRequestParams() {
        List<String> countries = List.of("AL", "AD", "AM", "AT", "BY", "BE", "BA", "BG", "CH", "CY", "CZ", "DE",
                "DK", "EE", "ES", "FO", "FI", "FR", "GB", "GE", "GI", "GR", "HU", "HR",
                "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MK", "MT", "NO", "NL", "PL",
                "PT", "RO", "RS", "RU", "SE", "SI", "SK", "SM", "TR", "UA", "VA");
        List<String> tracks = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        List<String> shipments = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");

        return new RequestParams(
                randomSubList(countries,
                        ThreadLocalRandom.current().nextInt(1, 10)),
                randomSubList(tracks, ThreadLocalRandom.current().nextInt(1, tracks.size() + 1)),
                randomSubList(shipments, ThreadLocalRandom.current().nextInt(1, shipments.size() + 1))
        );
    }

    private List<String> randomSubList(List<String> list, int newSize) {
        list = new ArrayList<>(list);
        Collections.shuffle(list);
        return list.subList(0, newSize);
    }
}