package br.com.senior.transport_logistics.domain.transport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<TransportEntity, Long> {

    @Query("""
            SELECT t 
            FROM Transport t
            where t.exitDay  between :startDate and :finishDate and t.driver.role = "DRIVER"
            """)
    List<TransportEntity> findAllByExitDay (LocalDate startDate, LocalDate finishDate);

    @Query("""
            SELECT t 
            FROM Transport t
            where t.exitDay  between :startDate and :finishDate and t.originHub.id = :idHub
            """)
    List<TransportEntity> findAllByExitDayAndOriginHub (LocalDate startDate, LocalDate finishDate, Long idHub);

}
