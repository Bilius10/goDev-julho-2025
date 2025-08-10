package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

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

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateRequestDTO employeeUpdateDTO){

        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, employeeUpdateDTO));
    }

    @PatchMapping("/password")
    public ResponseEntity<EmployeeResponseDTO> updatePassword(@AuthenticationPrincipal EmployeeEntity employee,
                                                              @RequestBody @Valid EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        service.updatePassword(employee, employeePasswordUpdateDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<EmployeeResponseDTO> updateRole(@PathVariable Long id, @RequestParam Role role) {
        service.updateRole(id, role);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<EmployeeResponseDTO> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/password")
    public ResponseEntity<EmployeeResponseDTO> updatePassword(@AuthenticationPrincipal EmployeeEntity employee, @RequestBody @Valid EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        service.updatePassword(employee, employeePasswordUpdateDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<EmployeeResponseDTO> updateRole(@PathVariable Long id, @RequestParam Role role) {
        service.updateRole(id, role);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
