package com.tnt.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tnt.model.AggregatedResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class ApiAggregationServiceTest {

    @Autowired
    private ApiAggregationService apiAggregationService;


    private List<String> randomSubList(List<String> list, int newSize) {
        list = new ArrayList<>(list);
        Collections.shuffle(list);
        return list.subList(0, newSize);
    }

    @Test
    public void testFetchSimple() {

        RequestParams requestParams = generateRequestParams();
        Mono<AggregatedResult> fetch = apiAggregationService.fetch(requestParams.getPricing(), requestParams.getTrack(), requestParams.getShipments());
        log.info("Result {}", fetch.block());
    }


    @Test
    public void testFetch() {

        int numberOfItems = 100;

        List<RequestParams> requests = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            requests.add(generateRequestParams());
        }

        List<Mono<AggregatedResult>> monos = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            RequestParams req = requests.get(i);
            monos.add(i, apiAggregationService
                .fetch(req.getPricing(), req.getTrack(), req.getShipments()));
        }

        for (int i = 0; i < numberOfItems; i++) {
            Mono<AggregatedResult> aggregatedResultMono = monos.get(i);
            RequestParams req = requests.get(i);
            StepVerifier
                .create(aggregatedResultMono)
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getShipments()).hasSize(req.getShipments().size());
                    Assertions.assertTrue(result.getShipments().keySet().containsAll(req.getShipments()));
                    assertThat(result.getPricing()).hasSize(req.getPricing().size());
                    Assertions.assertTrue(result.getPricing().keySet().containsAll(req.getPricing()));
                    assertThat(result.getTrack()).hasSize(req.getTrack().size());
                    Assertions.assertTrue(result.getTrack().keySet().containsAll(req.getTrack()));
                })
                .verifyComplete();
        }
    }


    @Test
    public void testCompleteIn10Seconds() {

        RequestParams req = generateRequestParams();

        Mono<AggregatedResult> mono = apiAggregationService.fetch(
           req.getPricing(),
            req.getTrack(),
            req.getShipments()
        );

        Duration duration = StepVerifier
            .create(mono)
            .thenConsumeWhile(result -> {
                assertThat(result).isNotNull();
                assertThat(result.getShipments()).hasSize(req.getShipments().size());
                Assertions.assertTrue(result.getShipments().keySet().containsAll(req.getShipments()));
                assertThat(result.getPricing()).hasSize(req.getPricing().size());
                Assertions.assertTrue(result.getPricing().keySet().containsAll(req.getPricing()));
                assertThat(result.getTrack()).hasSize(req.getTrack().size());
                Assertions.assertTrue(result.getTrack().keySet().containsAll(req.getTrack()));
                return true;
            })
            .verifyComplete();

        assertThat(duration).isLessThan(Duration.ofSeconds(10));
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
}
