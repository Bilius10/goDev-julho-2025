package br.com.senior.transport_logistics.domain.product.dto.response;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record ProductResponseDTO(
        long id,
        String name,
        ProductCategory category,
        double weight
) {

    public static ProductResponseDTO detailed(ProductEntity entity) {
        return ProductResponseDTO
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .weight(entity.getWeight())
                .build();

    }
}
