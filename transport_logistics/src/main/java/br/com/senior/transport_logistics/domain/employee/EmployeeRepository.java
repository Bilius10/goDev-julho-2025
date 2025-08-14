package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByCnh(String cnh);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    Optional<EmployeeEntity> findByEmail(String email);

    @Query("""
        SELECT d
        FROM Employee d
        WHERE d.hub.id = :idHub AND d.role = 'DRIVER'
        ORDER BY
            COALESCE(
                (SELECT SUM((CASE WHEN t.truck.id = :idTruck THEN 1 ELSE 0 END) + (CASE WHEN t.destinationHub.id = :idDestinationHub THEN 1 ELSE 0 END))
                 FROM Transport t
                 WHERE t.driver = d AND t.status = 'DELIVERED'),
            0L)
        DESC limit 1
    """)
    Optional<EmployeeEntity> findDriversOrderedByHistoryScore(Long idTruck, Long idDestinationHub, Long idHub);

    List<EmployeeEntity> findAllByRole(Role role);

    List<EmployeeEntity> findAllByRoleAndHub(Role role, HubEntity hub);

}
