package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.transport.TransportService;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.request.UpdateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportCreatedResponseDTO;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportResponseDTO;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
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
@RequestMapping("/api/v1/transports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@Tag(name = "TransportController", description = "Endpoints de criação, listagem, envio de relatórios, atualização e remoção de Transportes")
public class TransportController {

    private final TransportService service;

    @Operation(summary = "Endpoint para listar transportes com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista transportes paginados"),
    })
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

    @Operation(summary = "Endpoint buscar filial por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna filial desejada, de acordo com o ID informado"),
            @ApiResponse(responseCode = "404", description = "Nenhuma filial localizada com o ID informado")
    })
    @GetMapping("hubSummary/{id}")
    public ResponseEntity<HubSummaryProjection> hubSummary(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(service.hubSummary(id));
    }

    @Operation(summary = "Endpoint para criar transporte otimizado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transporte criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "404", description = "Origem, destino ou carga não encontrados")
    })
    @PostMapping("/optimize-allocation")
    public ResponseEntity<TransportCreatedResponseDTO> optimizeAllocation(@RequestBody @Valid CreateTransportRequest request) throws JsonProcessingException {
        TransportCreatedResponseDTO createdTransport = service.optimizeAllocation(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTransport.idTransport())
                .toUri();

        return ResponseEntity.created(location).body(createdTransport);
    }

    @Operation(summary = "Endpoint para envio manual de relatório semanal para caminhoneiros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório enviado por email com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao enviar emails")
    })
    @PostMapping("/send-weekly-schedule")
    public ResponseEntity<Void> sendWeeklySchedule() {
        service.sendWeeklySchedule();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Endpoint para envio manual de relatório mensal das filiais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório enviado por email com sucesso"),
    })
    @PostMapping("/send-month-report")
    public ResponseEntity<Void> sendMonthReport() {
        service.sendMonthReport();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Endpoint para confirmar transporte otimizado criado anteriormente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transporte confirmado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transporte não localizado com o ID informado")
    })
    @PatchMapping("/confirm-transport/{id}")
    public ResponseEntity<TransportResponseDTO> confirmTransport(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.confirmTransport(id));
    }

    @Operation(summary = "Endpoint para atualizar o status de um transporte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do transporte atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transporte não localizado com o ID informado")
    })
    @PatchMapping("/update-status/{id}")
    public ResponseEntity<TransportResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid TransportStatus status
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(service.updateStatus(id, status));
    }

    @Operation(summary = "Endpoint para atualizar um transporte")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transporte atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "404", description = "Transporte não localizado com o ID informado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTransportRequest updateTransportRequest
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(service.update(updateTransportRequest, id));
    }

    @Operation(summary = "Endpoint para remover um transporte (delete físico)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transporte removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transporte não localizado com o ID informado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
