package com.santik.service;

import com.santik.model.ApisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

@Service
@Slf4j
public class TrackService extends DataService<String> {

    public TrackService(WebClient trackClient, ApisProperties aggregatedApiProperties) {
        super(trackClient, aggregatedApiProperties.getTrack().getUri());
    }

    @Override
    public Function<Object, String> getDefaultObjectFunction() {
        return v -> "";
    }
}
