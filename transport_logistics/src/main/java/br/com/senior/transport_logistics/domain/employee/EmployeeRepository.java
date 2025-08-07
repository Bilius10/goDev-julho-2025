package br.com.senior.transport_logistics.domain.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByCnh(String cnh);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    Optional<EmployeeEntity> findByEmail(String email);
}
