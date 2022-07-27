package com.santik.configuration;


import com.santik.model.ApisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ClientsConfiguration {

    private final ApisProperties aggregatedApiProperties;

    @Bean
    public WebClient pricingClient() {
        ApisProperties.ApiProperties pricing = aggregatedApiProperties.getPricing();
        return WebClient.create(pricing.getBaseUrl());
    }

    @Bean
    public WebClient shipmentClient() {
        ApisProperties.ApiProperties shipments = aggregatedApiProperties.getShipments();
        return WebClient.create(shipments.getBaseUrl());
    }

    @Bean
    public WebClient trackClient() {
        ApisProperties.ApiProperties track = aggregatedApiProperties.getTrack();
        return WebClient.create(track.getBaseUrl());
    }
}
