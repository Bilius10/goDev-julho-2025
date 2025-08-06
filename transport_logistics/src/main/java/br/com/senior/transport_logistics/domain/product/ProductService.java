package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    public PageDTO<ProductResponseDTO> findAllWithFilters(ProductCategory category, Float limitWeight, Pageable pageable) {

        Page<ProductEntity> productsWithFilter
                = productRepository.findAllProductsWithFilters(category, limitWeight, pageable);

        return new PageDTO<>(
                productsWithFilter.map(p -> ProductResponseDTO(p.getId(), p.getName(), p.getCategory(), p.getWeight())),
                productsWithFilter.getNumber(),
                productsWithFilter.getSize(),
                productsWithFilter.getTotalElements(),
                productsWithFilter.getTotalPages()
        )
    }
}
