package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.dto.PageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService service;

    @PostMapping
    public ResponseEntity<TruckResponseDTO> create(@Valid @RequestBody TruckRequestDTO request) {
        var createdTruck = service.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(createdTruck.code())
                .toUri();

        return ResponseEntity.created(location).body(createdTruck);
    }

    @GetMapping
    public ResponseEntity<PageDTO<TruckResponseDTO>> getAll(
            @RequestParam(required = false) TruckStatus status,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "model", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.findAll(status, pageable));
    }

    @GetMapping("/{code}")
    public ResponseEntity<TruckResponseDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @PatchMapping("/{code}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String code,
            @RequestParam TruckStatus status
    ) {
        service.updateStatus(code, status);

        return ResponseEntity.noContent().build();
    }
}
