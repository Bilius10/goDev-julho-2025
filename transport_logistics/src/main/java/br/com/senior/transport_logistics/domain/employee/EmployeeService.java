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

import java.util.List;
import java.util.Objects;

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
    public EmployeeResponseDTO create(EmployeeCreateRequestDTO request) {
        createValidation(request);
        HubEntity hub = hubService.findById(request.idHub());

        String encode = passwordEncoder.encode(request.cpf());

        EmployeeEntity employeeEntity = new EmployeeEntity(request, hub, encode);

        EmployeeEntity savedEmployee = repository.save(employeeEntity);
        mailSenderService.sendWelcomeEmail(savedEmployee);

        return EmployeeResponseDTO.basic(savedEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO signIn(EmployeeLoginRequestDTO request) {
        var employee = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE_NOT_FOUND_BY_EMAIL.getMessage(request.email())));
        
        if(passwordEncoder.matches(request.password(), employee.getPassword())){
            mailSenderService.sendUpdatePasswordEmail(employee);
        }

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new WrongPasswordException("Senha informada incorreta.");
        }

        return EmployeeResponseDTO.token(tokenService.generateToken(employee));
    }

    @Transactional(readOnly = true)
    public PageDTO<EmployeeResponseDTO> findAll(Pageable pageable) {

        Page<EmployeeEntity> employees = repository.findAll(pageable);

        Page<EmployeeResponseDTO> employeesResponse = employees.map(EmployeeResponseDTO::basic);

        return new PageDTO<>(
                employeesResponse.getContent(),
                employees.getNumber(),
                employees.getSize(),
                employees.getTotalElements(),
                employees.getTotalPages());
    }

    @Transactional
    public EmployeeResponseDTO update(Long id, EmployeeUpdateRequestDTO request) {
        EmployeeEntity employeeFound = this.findById(id);

        if(!employeeFound.getEmail().equals(request.email())){
            verifyIfEmailIsUsed(request.email());
        }

        employeeFound.updateEmployee(request);

        EmployeeEntity savedEmployee = repository.save(employeeFound);

        return EmployeeResponseDTO.basic(savedEmployee);
    }

    @Transactional
    public void updatePassword(EmployeeEntity employee, EmployeePasswordUpdateDTO request) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new RuntimeException("Nova senha e confirmação não coincidem.");
        }

        if (!passwordEncoder.matches(request.currentPassword(), employee.getPassword())) {
            throw new WrongPasswordException("Senha atual incorreta.");
        }

        employee.setPassword(passwordEncoder.encode(request.newPassword()));

        repository.save(employee);
    }

    @Transactional
    public void updateRole(Long id, Role role) {
        EmployeeEntity employee = this.findById(id);

        employee.setRole(role);

        repository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        EmployeeEntity employeeFound = this.findById(id);
        employeeFound.setActive(false);

        repository.save(employeeFound);
    }

    public EmployeeEntity findDriversOrderedByHistoryScore(Long idTruck, Long idDestinationHub, Long idHub){
        return repository.findDriversOrderedByHistoryScore(idTruck, idDestinationHub, idHub)
                .orElseThrow(() -> new RuntimeException("Nenhum driver encontrado"));
    }

    public EmployeeEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EMPLOYEE_NOT_FOUND_BY_ID.getMessage(id)));
    }
  
    public List<EmployeeEntity> findAllByRole(Role role){
        return repository.findAllByRole(role);
    }

    public List<EmployeeEntity> findAllByRoleAndHub(Role role, HubEntity idHub){
        return repository.findAllByRoleAndHub(role, idHub);
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
