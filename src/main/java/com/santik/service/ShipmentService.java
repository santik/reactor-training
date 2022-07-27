package com.santik.service;

import com.santik.model.ApisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class ShipmentService extends DataService<List<String>> {

    public ShipmentService(WebClient shipmentClient, ApisProperties aggregatedApiProperties) {
        super(shipmentClient, aggregatedApiProperties.getShipments().getUri());
    }

    @Override
    public Function<Object, List<String>> getDefaultObjectFunction() {
        return v -> Collections.emptyList();
    }
}
