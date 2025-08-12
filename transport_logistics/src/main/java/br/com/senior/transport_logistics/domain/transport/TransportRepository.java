package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.transport.dto.response.HubSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query(value = """
        SELECT
            CAST(h.id AS BIGINT) AS hubId,
            h.name AS hubName,
            (h.street || ', ' || h.number || ' - ' || h.neighborhood || ', ' || h.city || ', ' || h.state) AS hubAddress,
            ARRAY_AGG(DISTINCT t.model) AS trucks,
            ARRAY_AGG(DISTINCT e.name) AS employees,
            COALESCE(SUM(ts.fuel_consumption), 0.00)::numeric AS totalFuelConsumption
        FROM
            hubs h
        LEFT JOIN
            transports ts ON h.id = ts.origin_hub_id
        LEFT JOIN
            trucks t ON ts.truck_id = t.id
        LEFT JOIN
            employees e ON h.id = e.hub_id
        WHERE
            h.id = :hubId
        GROUP BY
            h.id, h.name, h.street, h.number, h.neighborhood, h.city, h.state
        """, nativeQuery = true)
    Optional<HubSummaryProjection> findHubSummaryById(@Param("hubId") Long hubId);

}
