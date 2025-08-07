package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.Optional;

public interface HubRepository extends JpaRepository<HubEntity, Long> {

    boolean existsByName(String name);
    boolean existsByCity(String city);
    boolean existsByCnpj(String cnpj);

    @Query(value = """
        SELECT
            h.id AS hubId,
            h.name AS hubName,
            (h.street || ', ' || h.number || ' - ' || h.neighborhood || ', ' || h.city || ', ' || h.state) AS hubAddress,
            ARRAY_AGG(DISTINCT t.model) AS trucks,
            ARRAY_AGG(DISTINCT e.name) AS employees,
            COALESCE(SUM(ts.fuel_consumption), 0.00) AS totalFuelConsumption
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
