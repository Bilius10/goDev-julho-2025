package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController controller;

    @Mock
    private ProductService service;

    @Test
    @DisplayName("Deve retornar todos com filtragem")
    void findAllWithFilters() {
        ProductCategory category = ProductCategory.AUTOMOTIVE;
        Float limitWeight = 0.5f;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        boolean ascending = true;

        ProductEntity celular
                = new ProductEntity(1L, "Celular", ProductCategory.ELECTRONICS, 0.299, true);

        Page<ProductEntity> mockedPage = new PageImpl<>(Collections.singletonList(celular));

        List<ProductResponseDTO> list = mockedPage.map(p -> new ProductResponseDTO(p.getId(), p.getName(), p.getCategory(),
                p.getWeight())).toList();

        PageDTO<ProductResponseDTO> mockedPageDTO = new PageDTO<>(list, 1,1,1,1);

        when(service.findAllWithFilters(
                eq(category),
                eq(limitWeight),
                any(Pageable.class)
        )).thenReturn(mockedPageDTO);

        ResponseEntity<PageDTO<ProductResponseDTO>> response = controller.findAllWithFilters(
                category,
                limitWeight,
                page,
                size,
                sortBy,
                ascending
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockedPageDTO, response.getBody());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    @DisplayName("Deve retornar status created e o recurso")
    void create() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ProductRequestDTO testProduct = new ProductRequestDTO("nome teste", ProductCategory.AUTOMOTIVE, 0.200);
        ProductResponseDTO responseTestProduct = new ProductResponseDTO(1L, "nome teste", ProductCategory.AUTOMOTIVE, 0.200);

        when(service.create(testProduct)).thenReturn(responseTestProduct);

        ResponseEntity<ProductResponseDTO> create = controller.create(testProduct);

        assertEquals(create.getStatusCode(), HttpStatus.CREATED);
        assertEquals(create.getBody(), responseTestProduct);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Deve retornar o produto atualizado")
    void update() {

        Long productId = 1L;

        ProductRequestDTO testProduct
                = new ProductRequestDTO("nome teste", ProductCategory.AUTOMOTIVE, 0.200);

        ProductResponseDTO responseTestProduct =
                new ProductResponseDTO(1L, "nome teste", ProductCategory.AUTOMOTIVE, 0.200);

        when(service.update(productId, testProduct)).thenReturn(responseTestProduct);

        ResponseEntity<ProductResponseDTO> update = controller.update(productId, testProduct);

        assertEquals(update.getStatusCode(), HttpStatus.OK);
        assertEquals(update.getBody(), responseTestProduct);
    }

    @Test
    @DisplayName("Deve retornar no content")
    void delete() {

        Long productId = 1L;

        ResponseEntity<Void> delete = controller.delete(productId);

        assertEquals(delete.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(service).delete(productId);
    }
}
