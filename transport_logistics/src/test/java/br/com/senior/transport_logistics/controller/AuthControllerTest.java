package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeLoginRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController controller;

    @Mock
    private EmployeeService service;

    @Test
    @DisplayName("Deve retornar status CREATED e ")
    void create() {
        EmployeeCreateRequestDTO requestDTO = new EmployeeCreateRequestDTO("John Doe", "000", "000",
                "john.doe@example.com", 1L);

        when(service.create(requestDTO)).thenReturn(null);

        ResponseEntity<Void> response = controller.create(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(service).create(requestDTO);
    }

    @Test
    @DisplayName("Deve retornar status OK e realizar login com sucesso")
    void signIn() {
        EmployeeLoginRequestDTO requestDTO = new EmployeeLoginRequestDTO("john.doe@example.com", "password123");
        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO(1L, "John Doe", "000", "000",
                "john.doe@example.com", false, Role.DRIVER, null, null);

        when(service.signIn(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<EmployeeResponseDTO> response = controller.signIn(requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(service).signIn(requestDTO);
    }

    @Test
    @DisplayName("Deve retornar status NO CONTENT e trocar senha com sucesso")
    void updatePassword() {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setId(1L);

        EmployeePasswordUpdateDTO requestDTO = new EmployeePasswordUpdateDTO("oldPassword", "newPassword", "newPassword");

        doNothing().when(service).updatePassword(employee, requestDTO);

        ResponseEntity<Void> response = controller.updatePassword(employee, requestDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).updatePassword(employee, requestDTO);
    }
}
