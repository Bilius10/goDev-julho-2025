package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearer-key")
@Tag(name = "TruckController", description = "Endpoints de criação, listagem, busca por código e atualização do status")
public class TruckController {

    private final TruckService service;

    @Operation(summary = "Endpoint para criar caminhão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Caminhão criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
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

    @Operation(summary = "Endpoint para listar caminhões com paginação, podendo filtrar por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada das cargas"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
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

    @Operation(summary = "Endpoint para buscar caminhão por código")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna caminhão com código informado"),
            @ApiResponse(responseCode = "404", description = "Nenhum caminhão localizado com código informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @GetMapping("/{code}")
    public ResponseEntity<TruckResponseDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @Operation(summary = "Endpoint para atualizar status do caminhão via código")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou não fornecido"),
            @ApiResponse(responseCode = "404", description = "Caminhão não encontrada com o código informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PatchMapping("/{code}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String code,
            @RequestParam TruckStatus status
    ) {
        service.updateStatus(code, status);

        return ResponseEntity.noContent().build();
    }
}
