package br.com.senior.transport_logistics.domain.shipment;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Shipment")
@Table(name = "shipments")
public class ShipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{shipment.weight.notNull}")
    @Positive(message = "{shipment.weight.positive}")
    @Column(name = "weight", nullable = false)
    private Double weight;

    @NotNull(message = "{shipment.quantity.notNull}")
    @Min(value = 1, message = "{shipment.quantity.min}")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 100, message = "{shipment.notes.size}")
    @Column(name = "notes", length = 100)
    private String notes;

    @NotNull(message = "{shipment.isHazardous.notNull}")
    @Column(name = "is_hazardous", nullable = false)
    private boolean isHazardous;

    @NotNull(message = "{shipment.product.notNull}")
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity product;

    public ShipmentEntity(ShipmentCreateDTO request, ProductEntity product) {
        this.weight = request.quantity() * product.getWeight();
        this.quantity = request.quantity();
        this.notes = request.notes();
        this.isHazardous = request.isHazardous();
        this.product = product;
    }

    public void updateShipment(ShipmentUpdateDTO request, ProductEntity product) {

        if(!request.quantity().equals(this.quantity)) {
            this.quantity = request.quantity();
            this.weight = request.quantity() * product.getWeight();
        }

        this.notes = request.notes();
        this.isHazardous = request.isHazardous();
    }
}
