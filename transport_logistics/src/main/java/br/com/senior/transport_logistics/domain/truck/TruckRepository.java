package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {

    @Query("""
           SELECT t FROM Truck t
           WHERE (:status IS NULL OR t.status = :status)
           """)
    Page<TruckEntity> findAll(TruckStatus status, Pageable pageable);

    Optional<TruckEntity> findByCode(String code);
}
