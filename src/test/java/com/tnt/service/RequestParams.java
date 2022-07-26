package com.tnt.service;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class RequestParams {
    private List<String> pricing;
    private List<String> track;
    private List<String> shipments;
}
