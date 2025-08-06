package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.dto.PageDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private ProductRepository repository;

    public PageDTO<ProductResponseDTO> findAllWithFilters(ProductCategory category, Float limitWeight, Pageable pageable) {

        Page<ProductEntity> productsWithFilter
                = repository.findAllProductsWithFilters(category, limitWeight, pageable);

        return new PageDTO<>(
                productsWithFilter.map(p -> new ProductResponseDTO(p.getId(), p.getName(), p.getCategory(),
                        p.getWeight())).toList(),
                productsWithFilter.getNumber(),
                productsWithFilter.getSize(),
                productsWithFilter.getTotalElements(),
                productsWithFilter.getTotalPages()
        );
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request){

        if(repository.existsByNameIgnoreCase(request.name())){
            throw new RuntimeException("Já existe um produto com esse nome");
        }

        ProductEntity productEntity = new ProductEntity(request);

        ProductEntity saveProduct = repository.save(productEntity);

        return new ProductResponseDTO(
                saveProduct.getId(), saveProduct.getName(), saveProduct.getCategory(), saveProduct.getWeight()
        );
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO request){
        this.findById(id);

        ProductEntity productEntity = new ProductEntity(request);
        productEntity.setId(id);

        ProductEntity saveProduct = repository.save(productEntity);

        return new ProductResponseDTO(
                saveProduct.getId(), saveProduct.getName(), saveProduct.getCategory(), saveProduct.getWeight()
        );
    }

    @Transactional
    public void delete(Long id) {
        ProductEntity productFound = this.findById(id);
        productFound.setActive(false);

        repository.save(productFound);
    }

    private ProductEntity findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhum produto encontrado"));

    }

}
