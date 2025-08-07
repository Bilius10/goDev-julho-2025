package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.dto.PageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService service;

    @GetMapping
    public ResponseEntity<PageDTO<HubResponseDTO>> findAll(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ){
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HubSummaryProjection> hubSummary(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(service.hubSummary(id));
    }


    @PostMapping
    public ResponseEntity<HubResponseDTO> create(@RequestBody @Valid HubCreateRequestDTO hubCreateRequestDTO){
        HubResponseDTO createdHub = service.create(hubCreateRequestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdHub.id())
                .toUri();

        return ResponseEntity.created(location).body(createdHub);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HubResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid HubUpdateRequestDTO hubUpdateRequestDTO
            ){

        return ResponseEntity.status(HttpStatus.OK).body(service.update(hubUpdateRequestDTO, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
