package br.com.senior.transport_logistics.domain.employee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
