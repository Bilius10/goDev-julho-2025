package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.enums.AxleSetup;
import br.com.senior.transport_logistics.domain.truck.enums.TruckBody;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Truck")
@Table(name = "trucks")
public class TruckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "{truck.code.notBlank}")
    @Size(max = 50, message = "{truck.code.size}")
    @Column(name = "code", nullable = false)
    private String code;

    @NotBlank(message = "{truck.model.notBlank}")
    @Size(max = 100, message = "{truck.model.size}")
    @Column(name = "model", nullable = false)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    private HubEntity hub;

    @NotNull(message = "{truck.type.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TruckType type;

    @NotNull(message = "{truck.body.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "body", nullable = false)
    private TruckBody body;

    @NotNull(message = "{truck.axleSetup.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "axle_setup", nullable = false)
    private AxleSetup axleSetup;

    @NotNull(message = "{truck.loadCapacity.notNull}")
    @Positive(message = "{truck.loadCapacity.positive}")
    @Column(name = "load_capacity", nullable = false)
    private Double loadCapacity;

    @NotNull(message = "{truck.weight.notNull}")
    @Positive(message = "{truck.weight.positive}")
    @Column(name = "weight", nullable = false)
    private Double weight;

    @NotNull(message = "{truck.length.notNull}")
    @Positive(message = "{truck.length.positive}")
    @Column(name = "length", nullable = false)
    private Double length;

    @NotNull(message = "{truck.width.notNull}")
    @Positive(message = "{truck.width.positive}")
    @Column(name = "width", nullable = false)
    private Double width;

    @NotNull(message = "{truck.height.notNull}")
    @Positive(message = "{truck.height.positive}")
    @Column(name = "height", nullable = false)
    private Double height;

    @NotNull(message = "{truck.averageFuelConsumption.notNull}")
    @Positive(message = "{truck.averageFuelConsumption.positive}")
    @Column(name = "average_fuel_consumption", nullable = false)
    private Double averageFuelConsumption;

    @NotNull(message = "{truck.status.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TruckStatus status;

    @Size(max = 100, message = "{truck.features.size}")
    @Column(name = "features")
    private String features;

    public TruckEntity(TruckRequestDTO request, String code, HubEntity hub) {
        TruckEntity.builder()
                .code(code)
                .hub(hub)
                .model(request.model())
                .type(request.type())
                .body(request.body())
                .axleSetup(request.axleSetup())
                .loadCapacity(request.loadCapacity())
                .weight(request.weight())
                .length(request.length())
                .width(request.width())
                .height(request.height())
                .averageFuelConsumption(request.averageFuelConsumption())
                .features(request.features())
                .build();
    }
}
