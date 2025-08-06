package br.com.senior.transport_logistics.domain.product.dto.response;

import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponseDTO(
        long id,
        String name,
        ProductCategory category,
        double weight
) {

    public ProductResponseDTO(long id, String name, ProductCategory category, double weight) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.weight = weight;
    }
}
