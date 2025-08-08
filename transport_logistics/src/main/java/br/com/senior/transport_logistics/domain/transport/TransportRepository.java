package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends JpaRepository<TransportEntity, Long> {


}
