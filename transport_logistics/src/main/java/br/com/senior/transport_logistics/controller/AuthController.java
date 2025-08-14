package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeLoginRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@Tag(name = "AuthController", description = "Endpoints de criação de conta, login e atualização de senha")
public class AuthController {

    private final EmployeeService service;

    @Operation(summary = "Endpoint para criar funcionário e respectiva conta no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta de funcionário criada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos: não fornecidos, incorretos ou inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PostMapping("/create")
    public ResponseEntity<Void> create(@Valid @RequestBody EmployeeCreateRequestDTO dto) {
        service.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Endpoint para realizar login na conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Dados de login incorretos")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<EmployeeResponseDTO> signIn(@Valid @RequestBody EmployeeLoginRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.signIn(dto));
    }

    @Operation(summary = "Endpoint para realizar troca de senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Dados de login incorretos")
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal EmployeeEntity employee,
                                                              @RequestBody @Valid EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        service.updatePassword(employee, employeePasswordUpdateDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

