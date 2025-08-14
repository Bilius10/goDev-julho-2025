package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employees")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "EmployeeController", description = "Endpoints de listagem, atualização e remoção de funcionários")
public class EmployeeController {

    private final EmployeeService service;

    @Operation(summary = "Endpoint para listar funcionários com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada dos funcionários"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @GetMapping
    public ResponseEntity<PageDTO<EmployeeResponseDTO>> findAll(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @Operation(summary = "Endpoint para atualizar um funcionário via ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualização realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateRequestDTO employeeUpdateDTO) {

        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, employeeUpdateDTO));
    }

    @Operation(summary = "Endpoint para trocar Role de funcionário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado com o ID informado"),
            @ApiResponse(responseCode = "400", description = "Role não informada ou inválida"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @RequestParam Role role) {
        service.updateRole(id, role);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Endpoint para deletar funcionário (delete lógico)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso (lógico)"),
            @ApiResponse(responseCode = "404", description = "Nenhum funcionário localizado com o ID informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
