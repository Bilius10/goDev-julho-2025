package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private EmployeeRepository repository;

    private HubService hubService;


    public PageDTO<EmployeeResponseDTO> findAll(Pageable pageable) {

        Page<EmployeeEntity> employees = repository.findAll(pageable);

        return new PageDTO<>(employees.map(p -> new EmployeeResponseDTO(p.getId(), p.getName(), p.getCnh(), p.getCpf(), p.getEmail(), p.isActive(), p.getRole(), HubResponseDTO.basic(p.getHub()))).toList(),
                employees.getNumber(),
                employees.getSize(),
                employees.getTotalElements(),
                employees.getTotalPages());
    }


    @Transactional
    public EmployeeResponseDTO create(EmployeeCreateRequestDTO request) {

        createValidation(request);

        HubEntity hub = hubService.findById(request.idHub());
        EmployeeEntity employeeEntity = new EmployeeEntity(request, hub);

        EmployeeEntity saveEmployee = repository.save(employeeEntity);

        return new EmployeeResponseDTO(
                saveEmployee.getId(), saveEmployee.getName(), saveEmployee.getCnh(), saveEmployee.getCpf(), saveEmployee.getEmail(), saveEmployee.isActive(), saveEmployee.getRole(), HubResponseDTO.basic(saveEmployee.getHub())
        );
    }

    public EmployeeResponseDTO update(Long id, EmployeeUpdateRequestDTO request) {
        verifyIfEmailIsUsed(request.email());

        EmployeeEntity employeeFound = this.findById(id);
        employeeFound.updateEmployee(request);

        EmployeeEntity saveEmployee = repository.save(employeeFound);

        return new EmployeeResponseDTO(
                saveEmployee.getId(), saveEmployee.getName(), saveEmployee.getCnh(), saveEmployee.getCpf(), saveEmployee.getEmail(), saveEmployee.isActive(), saveEmployee.getRole(), HubResponseDTO.basic(saveEmployee.getHub())
        );
    }

    @Transactional
    public void delete(Long id) {
        EmployeeEntity employeeFound = this.findById(id);
        employeeFound.setActive(false);

        repository.save(employeeFound);
    }

    public EmployeeEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhum funcionário encontrado"));
    }

    private void createValidation(EmployeeCreateRequestDTO request) {
        verifyIfCnhIsUsed(request.cnh());
        verifyIfCpfIsUsed(request.cpf());
        verifyIfEmailIsUsed(request.email());
    }

    private void verifyIfCpfIsUsed(String cpf){
        if(repository.existsByCpf(cpf)){
            throw new RuntimeException("Cpf ja esta em uso");
        }
    }

    private void verifyIfEmailIsUsed(String email){
        if(repository.existsByEmail(email)){
            throw new RuntimeException("Email ja esta em uso");
        }
    }

    private void verifyIfCnhIsUsed(String cnh){
        if(repository.existsByCnh(cnh)){
            throw  new RuntimeException("Cnh ja esta em uso");
        }
    }
}
