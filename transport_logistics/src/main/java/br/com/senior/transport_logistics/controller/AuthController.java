package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeLoginRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeService service;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@Valid @RequestBody EmployeeCreateRequestDTO dto) {
        service.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<EmployeeResponseDTO> signIn(@Valid @RequestBody EmployeeLoginRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.signIn(dto));
    }

    @PatchMapping("/password")
    public ResponseEntity<EmployeeResponseDTO> updatePassword(@AuthenticationPrincipal EmployeeEntity employee,
                                                              @RequestBody @Valid EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        service.updatePassword(employee, employeePasswordUpdateDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

