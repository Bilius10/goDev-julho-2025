package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.shipment.ShipmentService;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.response.ShipmentResponseDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/shipments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@Tag(name = "ShipmentController", description = "Endpoints de criação, listagem, atualização e remoção física de cargas")
public class ShipmentController {

    private final ShipmentService service;

    @Operation(summary = "Endpoint para listar cargas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada das cargas"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @GetMapping
    public ResponseEntity<PageDTO<ShipmentResponseDTO>> findAll(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @Operation(summary = "Endpoint para criar carga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carga criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> create(@RequestBody @Valid ShipmentCreateDTO shipmentRequestDTO) {
        ShipmentResponseDTO createdShipment = service.create(shipmentRequestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdShipment.id())
                .toUri();

        return ResponseEntity.created(location).body(createdShipment);
    }

    @Operation(summary = "Endpoint para atualizar carga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carga atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "404", description = "Carga não encontrada com o ID informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ShipmentUpdateDTO shipmentRequestDTO) {

        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, shipmentRequestDTO));
    }

    @Operation(summary = "Endpoint para remover carga (delete físico)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carga deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carga não encontrada com o ID informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @DeleteMapping("/{id}")
    public  ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
