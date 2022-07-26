package com.tnt.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

        @Slf4j
        class SinkServiceTest {

            @Test
            void fetchDataTest() {

                TestSinkService testSinkService = new TestSinkService();
                testSinkService.startListen();

                int numberOfRequests = 5;

                List<Mono<Map<String, Double>>> monos = new ArrayList<>();
                List<List<String>> requests = new ArrayList<>();

                for (int i = 0; i < numberOfRequests; i++) {
                    List<String> request = generateRequest();
                    log.info("Requesting {}", request);
                    requests.add(request);
                    monos.add(testSinkService.fetchData(request));
                }


                for (int i = 0; i < numberOfRequests; i++) {
                    Mono<Map<String, Double>> response = monos.get(i);
                    List<String> request = requests.get(i);
                    StepVerifier.create(response).assertNext(stringDoubleMap -> {
                        log.info("Received in response {}", stringDoubleMap);
                        assertTrue(stringDoubleMap.keySet().containsAll(request));
                    }).verifyComplete();
                }
            }

            private List<String> generateRequest() {
                List<String> countries = List.of("AL", "AD", "AM", "AT", "BY", "BE", "BA", "BG", "CH", "CY", "CZ", "DE",
                        "DK", "EE", "ES", "FO", "FI", "FR", "GB", "GE", "GI", "GR", "HU", "HR",
                        "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC", "MK", "MT", "NO", "NL", "PL",
                        "PT", "RO", "RS", "RU", "SE", "SI", "SK", "SM", "TR", "UA", "VA");

                countries = new ArrayList<>(countries);
                Collections.shuffle(countries);

                return countries.subList(0, ThreadLocalRandom.current().nextInt(1, 10));
            }
        }