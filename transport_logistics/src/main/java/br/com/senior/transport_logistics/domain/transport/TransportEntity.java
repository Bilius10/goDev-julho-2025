
package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Transport")
@Table(name = "transports")
public class TransportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "{transport.fuelConsumption.notNull}")
    @Column(name = "fuel_consumption", nullable = false)
    private Double fuelConsumption;

    @Column(name = "distance")
    private Double distance;

    @NotNull(message = "{transport.exitDay.notNull}")
    @Column(name = "exit_day", nullable = false)
    private LocalDate exitDay;

    @NotNull(message = "{transport.expectedArrivalDay.notNull}")
    @Column(name = "expected_arrival_day", nullable = false)
    private LocalDate expectedArrivalDay;

    @NotNull(message = "{transport.status.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private TransportStatus status;

    @NotNull(message = "{transport.truck.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", referencedColumnName = "id")
    private TruckEntity truck;

    @NotNull(message = "{transport.shipment.notNull}")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private ShipmentEntity shipment;

    @NotNull(message = "{transport.driver.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private EmployeeEntity driver;

    @NotNull(message = "{transport.originHub.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_hub_id", referencedColumnName = "id")
    private HubEntity originHub;

    @NotNull(message = "{transport.destinationHub.notNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_hub_id", referencedColumnName = "id")
    private HubEntity destinationHub;
}
