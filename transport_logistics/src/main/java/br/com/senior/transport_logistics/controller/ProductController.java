package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearer-key")
@Tag(name = "ProductController", description = "Endpoints de criação, listagem, atualização e remoção lógica de produtos")
public class ProductController {

    private final ProductService service;

    @Operation(summary = "Endpoint para listar produtos com paginação, podendo ter filtros por categoria e peso limite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada dos funcionários"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @GetMapping
    public ResponseEntity<PageDTO<ProductResponseDTO>> findAllWithFilters(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Float limitWeight,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
            @RequestParam(defaultValue = "true", required = false) boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.status(HttpStatus.OK).body(service.findAllWithFilters(category, limitWeight, pageable));
    }

    @Operation(summary = "Endpoint para criar produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductRequestDTO productCreateDTO) {
        ProductResponseDTO createdProduct = service.create(productCreateDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.id())
                .toUri();

        return ResponseEntity.created(location).body(createdProduct);
    }

    @Operation(summary = "Endpoint para atualizar produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou não fornecidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado com o ID informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDTO productUpdateDTO) {

        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, productUpdateDTO));
    }

    @Operation(summary = "Endpoint para remover produto (delete lógico)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado com o ID informado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
