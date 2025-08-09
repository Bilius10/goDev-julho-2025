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
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.exception.common.WrongPasswordException;
import br.com.senior.transport_logistics.infrastructure.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final HubService hubService;
    private final SpringMailSenderService mailSenderService;

    @Transactional
    public EmployeeResponseDTO create(EmployeeCreateRequestDTO dto) {
        createValidation(dto);
        HubEntity hub = hubService.findById(dto.idHub());

        var employee = EmployeeEntity.builder()
                .name(dto.name())
                .cnh(dto.cnh())
                .cpf(dto.cpf())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.cpf()))
                .hub(hub)
                .active(true)
                .role(Role.DRIVER)
                .build();

        repository.save(employee);
        mailSenderService.sendWelcomeEmail(employee);
        return EmployeeResponseDTO.basic(employee, hub);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO signIn(EmployeeLoginRequestDTO dto) {
        var employee = repository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE_NOT_FOUND_BY_EMAIL.getMessage(dto.email())));

        if (!passwordEncoder.matches(dto.password(), employee.getPassword())) {
            throw new WrongPasswordException("Senha informada incorreta.");
        }

        return EmployeeResponseDTO.token(tokenService.generateToken(employee));
    }

    public PageDTO<EmployeeResponseDTO> findAll(Pageable pageable) {

        Page<EmployeeEntity> employees = repository.findAll(pageable);

        return new PageDTO<>(employees.map(p -> EmployeeResponseDTO.basic(p, p.getHub())).toList(),
                employees.getNumber(),
                employees.getSize(),
                employees.getTotalElements(),
                employees.getTotalPages());
    }

    public EmployeeResponseDTO update(Long id, EmployeeUpdateRequestDTO request) {
        verifyIfEmailIsUsed(request.email());

        EmployeeEntity employeeFound = this.findById(id);
        employeeFound.updateEmployee(request);

        EmployeeEntity saveEmployee = repository.save(employeeFound);

        return EmployeeResponseDTO.basic(saveEmployee, saveEmployee.getHub());
    }

    public EmployeeEntity findDriversOrderedByHistoryScore(Long idTruck, Long idDestinationHub, Long idHub){
        return repository.findDriversOrderedByHistoryScore(idTruck, idDestinationHub, idHub)
                .orElseThrow(() -> new RuntimeException("Nenhum driver encontrado"));
    }

    @Transactional
    public void delete(Long id) {
        EmployeeEntity employeeFound = this.findById(id);
        employeeFound.setActive(false);

        repository.save(employeeFound);
    }

    public EmployeeEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE_NOT_FOUND_BY_ID.getMessage(id)));
    }

    public void updatePassword(EmployeeEntity employee, @Valid EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        if (!employeePasswordUpdateDTO.newPassword().equals(employeePasswordUpdateDTO.confirmNewPassword())) {
            throw new RuntimeException("Nova senha e confirmação não coincidem.");
        }

        if (!passwordEncoder.matches(employeePasswordUpdateDTO.currentPassword(), employee.getPassword())) {
            throw new WrongPasswordException("Senha atual incorreta.");
        }

        employee.setPassword(passwordEncoder.encode(employeePasswordUpdateDTO.newPassword()));
        repository.save(employee);
    }

    @Transactional
    public void updateRole(Long id, Role role) {
        EmployeeEntity employee = this.findById(id);
        employee.setRole(role);
        repository.save(employee);
    }

    private void createValidation(EmployeeCreateRequestDTO request) {
        verifyIfCnhIsUsed(request.cnh());
        verifyIfCpfIsUsed(request.cpf());
        verifyIfEmailIsUsed(request.email());
    }

    private void verifyIfCpfIsUsed(String cpf){
        if(repository.existsByCpf(cpf)){
            throw new FieldAlreadyExistsException(EMPLOYEE_CPF_IN_USE.getMessage(cpf));
        }
    }

    private void verifyIfEmailIsUsed(String email){
        if(repository.existsByEmail(email)){
            throw new FieldAlreadyExistsException(EMPLOYEE_EMAIL_IN_USE.getMessage(email));
        }
    }

    private void verifyIfCnhIsUsed(String cnh){
        if(repository.existsByCnh(cnh)){
            throw new FieldAlreadyExistsException(EMPLOYEE_CNH_IN_USE.getMessage(cnh));
        }
    }
}
