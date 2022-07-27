package com.santik.service;

import com.santik.model.ApisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

@Slf4j
@Service
public class PricingService extends DataService<Double> {

    public PricingService(WebClient pricingClient, ApisProperties aggregatedApiProperties) {
        super(pricingClient, aggregatedApiProperties.getPricing().getUri());
    }

    @Override
    public Function<Object, Double> getDefaultObjectFunction() {
        return v -> Double.valueOf(0);
    }
}
