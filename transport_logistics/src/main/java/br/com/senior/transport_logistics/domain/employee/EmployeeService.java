package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
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

    public PageDTO<EmployeeResponseDTO> findAll(Pageable pageable) {

        Page<EmployeeEntity> employees = repository.findAll(pageable);

        return new PageDTO<>(employees.map(p -> new EmployeeResponseDTO(p.getId(), p.getName(), p.getCnh(), p.getCpf(), p.getEmail(), p.isActive(), p.getRole())).toList(),
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

        EmployeeEntity employeeEntity = new EmployeeEntity(request);

        EmployeeEntity saveEmployee = repository.save(employeeEntity);

        return new EmployeeResponseDTO(
                saveEmployee.getId(), saveEmployee.getName(), saveEmployee.getCnh(), saveEmployee.getCpf(), saveEmployee.getEmail(), saveEmployee.isActive(), saveEmployee.getRole()
        );
    }

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO request) {
        this.findById(id);

        EmployeeEntity employeeEntity = new EmployeeEntity(request);
        employeeEntity.setId(id);

        EmployeeEntity saveEmployee = repository.save(employeeEntity);

        return new EmployeeResponseDTO(
                saveEmployee.getId(), saveEmployee.getName(), saveEmployee.getCnh(), saveEmployee.getCpf(), saveEmployee.getEmail(), saveEmployee.isActive(), saveEmployee.getRole()
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
