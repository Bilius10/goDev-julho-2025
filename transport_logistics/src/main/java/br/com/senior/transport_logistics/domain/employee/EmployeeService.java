package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeRequestDTO;
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
    public EmployeeResponseDTO create(EmployeeRequestDTO request) {

        if(repository.existsByNameIgnoreCase(request.name())){
            throw new RuntimeException("Já existe um funcionário com esse nome");
        }

        HubEntity hub = hubService.findById(request.idHub());
        EmployeeEntity employeeEntity = new EmployeeEntity(request, hub);

        EmployeeEntity saveEmployee = repository.save(employeeEntity);

        return new EmployeeResponseDTO(
                saveEmployee.getId(), saveEmployee.getName(), saveEmployee.getCnh(), saveEmployee.getCpf(), saveEmployee.getEmail(), saveEmployee.isActive(), saveEmployee.getRole(), HubResponseDTO.basic(saveEmployee.getHub())
        );
    }

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO request) {
        this.findById(id);

        HubEntity hub = hubService.findById(request.idHub());
        EmployeeEntity employeeEntity = new EmployeeEntity(request, hub);
        employeeEntity.setId(id);

        EmployeeEntity saveEmployee = repository.save(employeeEntity);

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
}
