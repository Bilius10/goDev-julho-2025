package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {

    @Query("""
           SELECT t FROM Truck t
           WHERE (:status IS NULL OR t.status = :status)
           """)
    Page<TruckEntity> findAll(TruckStatus status, Pageable pageable);

    Optional<TruckEntity> findByCode(String code);

    @Query("""
        select  avg(t.weight) as weightAvarege, 
                avg(t.length) as lengthAvarege,
                avg(t.height) as heightAvarege
            from Truck t
    """)
    Optional<AverageDimensionsTrucks> findAverageDimensionsTrucks();

    @Query("""
    SELECT t
    FROM Truck t
    WHERE t.loadCapacity > :loadCapacity
      AND t.hub.id = :idHub
      AND t.status = 'AVAILABLE'
      AND t.id NOT IN (
          SELECT tr.truck.id
          FROM Transport tr
          WHERE tr.status <> 'DELIVERED'
            AND (
                (tr.exitDay BETWEEN :exitDay AND :returnDay)
                OR (tr.expectedArrivalDay BETWEEN :exitDay AND :returnDay)
                OR (:exitDay BETWEEN tr.exitDay AND tr.expectedArrivalDay)
                OR (:returnDay BETWEEN tr.exitDay AND tr.expectedArrivalDay)
            )
      )
    """)
    List<TruckEntity> findAvailableTrucksByCapacityAndHubNotInRouteBetween(
            @Param("loadCapacity") Double loadCapacity,
            @Param("idHub") Long idHub,
            @Param("exitDay") LocalDate exitDay,
            @Param("returnDay") LocalDate returnDay
    );

    List<TruckEntity> findAllByHub(HubEntity hub);




}
