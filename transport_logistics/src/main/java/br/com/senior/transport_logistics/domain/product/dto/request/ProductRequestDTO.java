package br.com.senior.transport_logistics.domain.product.dto.request;

import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO(

        @NotBlank(message = "{product.name.notBlank}")
        @Size(max = 100, message = "{product.name.size}")
        String name,

        @NotNull(message = "{product.category.notNull}")
        ProductCategory productCategory,

        @NotNull(message = "{product.weight.notNull}")
        double weight
) {
}
