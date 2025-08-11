package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    @GetMapping
    public ResponseEntity<PageDTO<ProductResponseDTO>> findAllWithFilters(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Float limitWeight,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ){
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAllWithFilters(category, limitWeight, pageable));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductRequestDTO productCreateDTO){

        ProductResponseDTO createdProduct = service.create(productCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.id())
                .toUri();

        return ResponseEntity.created(location).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDTO productUpdateDTO){

        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, productUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){

        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
