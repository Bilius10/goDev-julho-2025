package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeLoginRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @InjectMocks
    private EmployeeController controller;

    @Mock
    private EmployeeService service;

    @Test
    @DisplayName("Deve retornar status OK e listar funcionários com paginação")
    void findAll() {
        int page = 0;
        int size = 10;

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO(1L, "John Doe", "000", "000",
                "john.doe@example.com", false, Role.DRIVER, null, null);

        Page<EmployeeResponseDTO> employeePage = new PageImpl<>(Collections.singletonList(responseDTO));
        PageDTO<EmployeeResponseDTO> pageDTO = new PageDTO<>(employeePage.getContent(), employeePage.getNumber(), employeePage.getSize(), employeePage.getTotalElements(), employeePage.getTotalPages());

        when(service.findAll(any(Pageable.class))).thenReturn(pageDTO);

        ResponseEntity<PageDTO<EmployeeResponseDTO>> response = controller.findAll(page, size, "name", true);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    @DisplayName("Deve retornar status OK e DTO do funcionário atualizado")
    void update() {
        Long employeeId = 1L;
        EmployeeUpdateRequestDTO requestDTO = new EmployeeUpdateRequestDTO("John Doe Updated", "john.doe@example.com");
        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO(1L, "John Doe", "000", "000",
                "john.doe@example.com", false, Role.DRIVER, null, null);

        when(service.update(employeeId, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<EmployeeResponseDTO> response = controller.update(employeeId, requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(service).update(employeeId, requestDTO);
    }

    @Test
    @DisplayName("Deve retornar status NO CONTENT e trocar role com sucesso")
    void updateRole() {
        Long employeeId = 1L;
        Role newRole = Role.ADMIN;

        doNothing().when(service).updateRole(employeeId, newRole);

        ResponseEntity<Void> response = controller.updateRole(employeeId, newRole);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).updateRole(employeeId, newRole);
    }

    @Test
    @DisplayName("Deve retornar status NO CONTENT e fazer delete lógico com sucesso")
    void delete() {
        Long employeeId = 1L;

        doNothing().when(service).delete(employeeId);

        ResponseEntity<Void> response = controller.delete(employeeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).delete(employeeId);
    }
}
