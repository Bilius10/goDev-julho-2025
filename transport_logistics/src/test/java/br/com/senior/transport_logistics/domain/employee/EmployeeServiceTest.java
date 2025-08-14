package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeLoginRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeePasswordUpdateDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.email.SpringMailSenderService;
import br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages;
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.exception.common.WrongPasswordException;
import br.com.senior.transport_logistics.infrastructure.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private HubService hubService;

    @Mock
    private SpringMailSenderService mailSenderService;

    @InjectMocks
    private EmployeeService service;

    private HubEntity hub;
    private Pageable pageable;
    private List<EmployeeEntity> employees;
    private Page<EmployeeEntity> employeesPage;


    @BeforeEach
    void setUp() {
        hub = new HubEntity();
        hub.setId(1L);

        pageable = PageRequest.of(0, 10);

        employees = List.of(
                createEmployeeEntity("Employee 1", "638.391.710-24", "employee1@email.com", hub),
                createEmployeeEntity("Employee 2", "927.521.000-43", "employee2@email.com", hub),
                createEmployeeEntity("Employee 3", "287.946.160-00", "employee3@email.com", hub)
        );

        employeesPage = new PageImpl<>(employees, pageable, 3);

    }

    @Test
    @DisplayName("Deve criar um employee com sucesso")
    void signUp_shouldCreateEmployee() {
        when(hubService.findById(1L)).thenReturn(hub);

        var employee = createEmployeeEntity();
        var request = createEmployeeRequestDTO();

        when(repository.existsByCnh(employee.getCnh())).thenReturn(false);
        when(repository.existsByCpf(employee.getCpf())).thenReturn(false);
        when(repository.existsByEmail(employee.getEmail())).thenReturn(false);

        when(repository.save(any(EmployeeEntity.class))).thenReturn(employee);

        var response = service.create(request);

        assertNotNull(response);
        assertEquals(response.name(), employee.getName());
        assertEquals(response.cnh(), employee.getCnh());
        assertEquals(response.cpf(), employee.getCpf());
        assertEquals(response.email(), employee.getEmail());
        assertEquals(response.hub().id(), employee.getHub().getId());
        assertEquals(response.role(), employee.getRole());

        verify(repository).existsByEmail(employee.getEmail());
        verify(repository).existsByCpf(employee.getCpf());
        verify(repository).existsByCnh(employee.getCnh());
    }

    @Test
    @DisplayName("Deve lançar FieldAlreadyExistsException quando o email existir")
    void signUp_shouldNotCreateBecauseEmailAlreadyExists() {
        var employee = createEmployeeEntity();
        var request = createEmployeeRequestDTO();

        when(repository.existsByEmail(employee.getEmail())).thenReturn(true);

        var exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(request));

        assertEquals(ExceptionMessages.EMPLOYEE_EMAIL_IN_USE.getMessage(employee.getEmail()), exception.getMessage());

        verify(repository).existsByEmail(employee.getEmail());
    }

    @Test
    @DisplayName("Deve lançar FieldAlreadyExistsException quando o cpf existir")
    void signUp_shouldNotCreateBecauseCPFAlreadyExists() {
        var employee = createEmployeeEntity();
        var request = createEmployeeRequestDTO();

        when(repository.existsByCpf(employee.getCpf())).thenReturn(true);

        var exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(request));

        assertEquals(ExceptionMessages.EMPLOYEE_CPF_IN_USE.getMessage(employee.getCpf()), exception.getMessage());

        verify(repository).existsByCpf(employee.getCpf());
    }

    @Test
    @DisplayName("Deve lançar FieldAlreadyExistsException quando a cnh existir")
    void signUp_shouldNotCreateBecauseCNHAlreadyExists() {
        var employee = createEmployeeEntity();
        var request = createEmployeeRequestDTO();

        when(repository.existsByCnh(employee.getCnh())).thenReturn(true);

        var exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(request));

        assertEquals(ExceptionMessages.EMPLOYEE_CNH_IN_USE.getMessage(employee.getCnh()), exception.getMessage());

        verify(repository).existsByCnh(employee.getCnh());
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void signIn_shouldLogin() {
        var employee = createEmployeeEntity();
        var request = createEmployeeLoginRequestDTO();

        when(repository.findByEmail(request.email())).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(request.password(), employee.getPassword())).thenReturn(true);
        when(tokenService.generateToken(employee)).thenReturn("token");

        var response =  service.signIn(request);

        assertEquals("token", response.token());

        verify(repository).findByEmail(request.email());
    }

    @Test
    @DisplayName("Deve lançar WrongPasswordException ao realizar login com senha errada")
    void signIn_shouldThrowWrongPasswordException() {
        var employee = createEmployeeEntity();
        var request = createEmployeeLoginRequestDTO();

        when(repository.findByEmail(request.email())).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(request.password(), employee.getPassword())).thenReturn(false);

        var exception =  assertThrows(WrongPasswordException.class, () -> service.signIn(request));
        assertEquals(ExceptionMessages.EMPLOYEE_WRONG_CURRENT_PASSWORD.getMessage(), exception.getMessage());

        verify(repository).findByEmail(request.email());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao realizar login com email inexistente")
    void signIn_shouldThrowResourceNotFoundException() {
        var request = createEmployeeLoginRequestDTO();

        when(repository.findByEmail(request.email())).thenReturn(Optional.empty());

        var exception =  assertThrows(ResourceNotFoundException.class, () -> service.signIn(request));

        assertEquals(ExceptionMessages.EMPLOYEE_NOT_FOUND_BY_EMAIL.getMessage(request.email()), exception.getMessage());

        verify(repository).findByEmail(request.email());
    }

    @Test
    @DisplayName("Deve retornar página com employees quando existem dados")
    void findAll_shouldReturnPageWithCustomersWhenDataExists() {
        when(repository.findAll(pageable)).thenReturn(employeesPage);

        PageDTO<EmployeeResponseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(3, result.data().size());
        assertEquals(10, result.size());
        assertEquals(3, result.totalElements());
        assertEquals(1, result.totalPages());

        assertEquals("Employee 1", result.data().get(0).name());
        assertEquals("638.391.710-24", result.data().get(0).cpf());
        assertEquals(hub.getId(), result.data().get(0).hub().id());

        assertEquals("Employee 2", result.data().get(1).name());
        assertEquals("927.521.000-43", result.data().get(1).cpf());

        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não existem dados")
    void findAll_shouldReturnEmptyPageWhenNoDataExists() {
        Page<EmployeeEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        PageDTO<EmployeeResponseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.data().size());
        assertEquals(10, result.size());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve atualizar com todos os campos sendo diferentes quando email existir e dados forem válidos")
    void update_shouldUpdateAllFields() {
        Long id = 1L;

        var employee = createEmployeeEntity();
        var request = new EmployeeUpdateRequestDTO("Employee Updated", "employeeupdated@email.com");

        when(repository.findById(id)).thenReturn(Optional.of(employee));
        when(repository.save(employee)).thenReturn(employee);

        var response = service.update(id, request);

        assertNotNull(response);
        assertEquals(response.name(), request.name());
        assertEquals(response.email(), request.email());

        verify(repository).findById(id);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve atualizar campo nome, pois outros são iguais, quando email existir e dados forem válidos")
    void update_shouldUpdateOnlyName() {
        Long id = 1L;

        var employee = createEmployeeEntity();
        var request = new EmployeeUpdateRequestDTO("Employee Updated", "employee@email.com");

        when(repository.findById(id)).thenReturn(Optional.of(employee));
        when(repository.save(employee)).thenReturn(employee);

        var response = service.update(id, request);

        assertNotNull(response);
        assertEquals(response.name(), request.name());
        assertEquals(response.email(), request.email());

        verify(repository).findById(id);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve deletar employee")
    void delete_shouldDelete() {
        Long id = 1L;
        EmployeeEntity employee = createEmployeeEntity();

        when(repository.findById(id)).thenReturn(Optional.of(employee));

        service.delete(id);

        assertFalse(employee.isActive());
        verify(repository).save(employee);

    }

    @Test
    @DisplayName("Deve obter EmployeeEntity ao buscar por ID existente")
    void findById_shouldReturnById() {
        var employee = createEmployeeEntity();

        when(repository.findById(employee.getId())).thenReturn(Optional.of(employee));

        EmployeeEntity response = service.findById(employee.getId());

        assertNotNull(response);

        assertEquals(response.getName(), employee.getName());
        assertEquals(response.getCpf(), employee.getCpf());
        assertEquals(response.getEmail(), employee.getEmail());
        assertEquals(response.getCnh(), employee.getCnh());
        assertEquals(response.getRole(), employee.getRole());
        verify(repository).findById(employee.getId());
    }

    @Test
    @DisplayName("Buscar employee por ID inexistente")
    void findById_shouldThrowResourceNotFoundException() {
        var employee = createEmployeeEntity();

        when(repository.findById(employee.getId())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.findById(employee.getId()));

        assertEquals(ExceptionMessages.EMPLOYEE_NOT_FOUND_BY_ID.getMessage(employee.getId()), exception.getMessage());

        verify(repository).findById(employee.getId());
    }

    @Test
    @DisplayName("Deve lançar WrongPasswordException quando senha nova e confirmação são diferentes")
    void updatePassword_shouldThrowWrongPasswordExceptionWhenPasswordsMismatch() {
        var employee = createEmployeeEntity();

        var request = new EmployeePasswordUpdateDTO("12345679", "12345678", "12345677");
        var exception = assertThrows(WrongPasswordException.class, () -> service.updatePassword(employee, request));

        assertEquals(ExceptionMessages.EMPLOYEE_PASSWORD_CONFIRMATION_MISMATCH.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar WrongPasswordException quando senha atual está incorreta")
    void updatePassword_shouldThrowWrongPasswordExceptionExceptionWhenCurrentPasswordIsWrong() {
        var employee = createEmployeeEntity();
        employee.setPassword(passwordEncoder.encode("12345678"));

        var request = new EmployeePasswordUpdateDTO("12345679", "12345678", "12345678");
        var exception = assertThrows(WrongPasswordException.class, () -> service.updatePassword(employee, request));

        assertEquals(ExceptionMessages.EMPLOYEE_WRONG_CURRENT_PASSWORD.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar a senha corretamente")
    void updatePassword_shouldUpdatePassword() {
        var currentPassword = "12345678";
        var newPassword = "11111111";
        var encodedCurrentPassword = passwordEncoder.encode(currentPassword);

        var employee = mock(EmployeeEntity.class);
        when(employee.getPassword()).thenReturn(encodedCurrentPassword);

        var request = new EmployeePasswordUpdateDTO(currentPassword, newPassword, newPassword);

        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        service.updatePassword(employee, request);

        verify(employee).setPassword("encodedNewPassword");
        verify(repository).save(employee);
    }

    @Test
    @DisplayName("Deve atualizar cargo corretamente")
    void updateRole_shouldUpdateRole() {
        var employee = createEmployeeEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        service.updateRole(1L, Role.MANAGER);

        verify(repository).save(employee);
        assertEquals(Role.MANAGER, employee.getRole());
    }

    @Test
    @DisplayName("Deve lançar exceção quando employee não for encontrado")
    void updateRole_shouldNotUpdateRole() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.updateRole(1L, Role.MANAGER));
        assertEquals(ExceptionMessages.EMPLOYEE_NOT_FOUND_BY_ID.getMessage(1L), exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando motorista não for encontrado")
    void findDriversOrderedByHistoryScore_shouldThrowExceptionWhenNotFound() {
        var id = 1L;
        when(repository.findDriversOrderedByHistoryScore(id, id, id)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.findDriversOrderedByHistoryScore(id, id, id));
        assertEquals(ExceptionMessages.DRIVER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar motoristas corretamente")
    void findDriversOrderedByHistoryScore_shouldReturnDriversList() {
        var id = 1L;
        var employee = createEmployeeEntity();
        when(repository.findDriversOrderedByHistoryScore(id, id, id)).thenReturn(Optional.of(employee));

        var response = service.findDriversOrderedByHistoryScore(id, id, id);

        assertEquals(employee, response);
        verify(repository).findDriversOrderedByHistoryScore(id, id, id);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando motoristas não forem encontrados")
    void findAllByRole_shouldThrowExceptionWhenNotFound() {
        var role = Role.MANAGER;
        when(repository.findAllByRole(role)).thenReturn(List.of());

        var response = service.findAllByRole(role);
        assertEquals(0, response.size());
        assertNotNull(response);
        verify(repository).findAllByRole(role);
    }

    @Test
    @DisplayName("Deve retornar motoristas corretamente")
    void findAllByRole_shouldReturnDriversList() {
        var role = Role.MANAGER;
        var employee = createEmployeeEntity();
        when(repository.findAllByRole(role)).thenReturn(List.of(employee));

        var response = service.findAllByRole(role);
        assertEquals(1, response.size());
        assertEquals(employee, response.get(0));
        verify(repository).findAllByRole(role);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando motoristas não forem encontrados")
    void findAllByRoleAndHub_shouldThrowExceptionWhenNotFound() {
        var role = Role.MANAGER;
        var hub = new HubEntity();
        when(repository.findAllByRoleAndHub(role, hub)).thenReturn(List.of());

        var response = service.findAllByRoleAndHub(role, hub);
        assertEquals(0, response.size());
        assertNotNull(response);
        verify(repository).findAllByRoleAndHub(role, hub);
    }

    @Test
    @DisplayName("Deve retornar motoristas corretamente")
    void findAllByRoleAndHub_shouldReturnDriversList() {
        var role = Role.MANAGER;
        var hub = new HubEntity();
        var employee = createEmployeeEntity();
        when(repository.findAllByRoleAndHub(role, hub)).thenReturn(List.of(employee));

        var response = service.findAllByRoleAndHub(role, hub);
        assertEquals(1, response.size());
        assertEquals(employee, response.get(0));
        verify(repository).findAllByRoleAndHub(role, hub);
    }


    private EmployeeEntity createEmployeeEntity() {
        return EmployeeEntity.builder()
                .id(1L)
                .name("Employee")
                .cnh("12145676101")
                .cpf("685.928.470-60")
                .email("employee@email.com")
                .hub(hub)
                .role(Role.DRIVER)
                .active(true)
                .build();
    }

    private EmployeeEntity createEmployeeEntity(String name, String cpf, String email, HubEntity hub) {
        return EmployeeEntity.builder()
                .name(name)
                .cpf(cpf)
                .email(email)
                .hub(hub)
                .active(true)
                .build();
    }


    private EmployeeCreateRequestDTO createEmployeeRequestDTO() {
        return new EmployeeCreateRequestDTO(
                "Employee",
                "12145676101",
                "685.928.470-60",
                "employee@email.com",
                1L
        );
    }

    private EmployeeLoginRequestDTO createEmployeeLoginRequestDTO() {
        return new EmployeeLoginRequestDTO(
                "employee@email.com",
                "securePassword123"
        );
    }
}