package br.com.senior.transport_logistics.domain.shipment;

import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {

    @Query("""
        SELECT s FROM Shipment s where s.status = :status and s.originHub.id = :idOriginHub and s.destinationHub.id = :idDestinationHub
        """)
    List<ShipmentEntity> findAllByIdHubAndDestinationHubAndStatus(TransportStatus status,
                                                                  Long idOriginHub,
                                                                  Long idDestinationHub);

}
