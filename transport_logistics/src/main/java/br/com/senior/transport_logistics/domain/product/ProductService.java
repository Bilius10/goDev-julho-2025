package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.PRODUCT_NAME_IN_USE;
import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.PRODUCT_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional(readOnly = true)
    public PageDTO<ProductResponseDTO> findAllWithFilters(ProductCategory category, Float limitWeight, Pageable pageable) {

        Page<ProductEntity> productsWithFilter
                = repository.findAllProductsWithFilters(category, limitWeight, pageable);

        Page<ProductResponseDTO> productResponse = productsWithFilter.map(ProductResponseDTO::detailed);

        return new PageDTO<>(
                productResponse.getContent(),
                productsWithFilter.getNumber(),
                productsWithFilter.getSize(),
                productsWithFilter.getTotalElements(),
                productsWithFilter.getTotalPages()
        );
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request){

        if(repository.existsByNameIgnoreCase(request.name())){
            throw new FieldAlreadyExistsException(PRODUCT_NAME_IN_USE.getMessage(request.name()));
        }

        ProductEntity productEntity = new ProductEntity(request);

        ProductEntity savedProduct = repository.save(productEntity);

        return ProductResponseDTO.detailed(savedProduct);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO request){
        this.findById(id);

        ProductEntity productEntity = new ProductEntity(request);
        productEntity.setId(id);

        ProductEntity savedProduct = repository.save(productEntity);

        return ProductResponseDTO.detailed(savedProduct);
    }

    @Transactional
    public void delete(Long id) {
        ProductEntity productFound = this.findById(id);
        productFound.setActive(false);

        repository.save(productFound);
    }

    @Transactional(readOnly = true)
    public ProductEntity findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_BY_ID.getMessage(id)));
    }

}
