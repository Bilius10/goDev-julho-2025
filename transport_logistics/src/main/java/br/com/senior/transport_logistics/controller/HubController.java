package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
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
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@Tag(name = "HubController", description = "Endpoints de criação, listagem, atualização e remoção de filiais")
public class HubController {

    private final HubService service;

    @Operation(summary = "Endpoint para listar filiais com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada das filiais")
    })
    @GetMapping
    public ResponseEntity<PageDTO<HubResponseDTO>> findAll(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
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
    @GetMapping("/{id}")
    public ResponseEntity<HubSummaryProjection> hubSummary(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.hubSummary(id));
    }

    @Operation(summary = "Endpoint para criar uma filial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Realiza a criação de uma nova filial"),
            @ApiResponse(responseCode = "409", description = "Já existe uma filial com o mesmo nome, CNPJ ou na mesma cidade")
    })
    @PostMapping
    public ResponseEntity<HubResponseDTO> create(@RequestBody @Valid HubCreateRequestDTO hubCreateRequestDTO) {
        HubResponseDTO createdHub = service.create(hubCreateRequestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdHub.id())
                .toUri();

        return ResponseEntity.created(location).body(createdHub);
    }

    @Operation(summary = "Endpoint para atualizar endereço de filial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filial foi atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou não informados"),
            @ApiResponse(responseCode = "404", description = "Nenhuma filial localizada com o ID informado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HubResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid HubUpdateRequestDTO hubUpdateRequestDTO
    ) {

        return ResponseEntity.status(HttpStatus.OK).body(service.update(hubUpdateRequestDTO, id));
    }

    @Operation(summary = "Endpoint para remover uma filial (delete físico)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filial foi deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma filial localizada com o ID informado"),
            @ApiResponse(responseCode = "409", description = "Filial tem outras entidades envolvidas")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
