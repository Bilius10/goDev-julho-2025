package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Entity(name = "Product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Size(max = 100, message = "{product.name.size}")
    @NotBlank(message = "{product.name.notBlank}")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{product.category.notNull}")
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    @NotNull(message = "{product.weight.notNull}")
    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "active")
    private boolean active;

    public ProductEntity(ProductRequestDTO productCreateDTO) {
        this.name = productCreateDTO.name();
        this.category = productCreateDTO.productCategory();
        this.weight = productCreateDTO.weight();
        this.active = true;
    }

}
