package br.com.senior.transport_logistics.domain.truck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository repository;

}
