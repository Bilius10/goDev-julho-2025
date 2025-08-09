package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.transport.TransportService;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.request.UpdateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportResponseDTO;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.GeminiResponse;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService service;

    @GetMapping
    public ResponseEntity<PageDTO<TransportResponseDTO>> findAll(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @PostMapping("/optimize-allocation")
    public ResponseEntity<TransportResponseDTO> optimizeAllocation(@RequestBody @Valid CreateTransportRequest request) throws JsonProcessingException {
        TransportResponseDTO createdTransport = service.optimizeAllocation(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTransport.id())
                .toUri();

        return ResponseEntity.created(location).body(createdTransport);
    }

    @PostMapping("/send-weekly-schedule")
    public ResponseEntity<Void> sendWeeklySchedule(){
        service.sendWeeklySchedule();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/send-month-report")
    public ResponseEntity<Void> sendMonthReport(){
        service.sendMonthReport();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/confirm-transport/{id}")
    public ResponseEntity<TransportResponseDTO> confirmTransport(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(service.confirmTransport(id));
    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<TransportResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid TransportStatus status
    ){
        return ResponseEntity.status(HttpStatus.OK).body(service.updateStatus(id, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTransportRequest updateTransportRequest
    ){

        return ResponseEntity.status(HttpStatus.OK).body(service.update(updateTransportRequest, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
