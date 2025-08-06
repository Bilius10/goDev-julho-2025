package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hubs")
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
}
