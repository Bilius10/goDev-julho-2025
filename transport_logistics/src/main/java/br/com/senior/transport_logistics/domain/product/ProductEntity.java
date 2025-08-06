package br.com.senior.transport_logistics.domain.product;

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

    @Column(name = "name")
    @Size(max = 100, message = "{product.name.size}")
    @NotBlank(message = "{product.name.notBlank}")
    private String name;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{product.category.notNull}")
    @Size(max = 100, message = "{product.category.size}")
    private ProductCategory category;

    @Column(name = "weight")
    @NotNull(message = "")
    private float weight;

}
