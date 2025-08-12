package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.employee.EmployeeRepository;
import br.com.senior.transport_logistics.domain.product.dto.request.ProductRequestDTO;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages;
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Test
    @DisplayName("Deve listar todos usando filtros")
    void findAllWithFilters() {

        ProductCategory category = ProductCategory.AUTOMOTIVE;
        Float limitWeight = 1.5f;

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductEntity> productEntities = List.of(
                new ProductEntity(1L, "Pneu", ProductCategory.AUTOMOTIVE, 1.2f, true),
                new ProductEntity(2L, "Filtro de Ar", ProductCategory.AUTOMOTIVE, 0.5f, true)
        );

        Page<ProductEntity> productsPage = new PageImpl<>(productEntities, pageable, 2);

        when(repository.findAllProductsWithFilters(category, limitWeight, pageable))
                .thenReturn(productsPage);

        PageDTO<ProductResponseDTO> result = service.findAllWithFilters(category, limitWeight, pageable);

        assertEquals(result.data().get(0).name(), productEntities.get(0).getName());
        assertEquals(result.data().get(1).weight(), productEntities.get(1).getWeight());
        assertEquals(result.totalElements(), 2);
    }

    @Test
    @DisplayName("Caso em que criamos um produto")
    void create_context1() {

        ProductEntity produtoOriginal
                = new ProductEntity(1L, "Celular Teste novo", ProductCategory.ELECTRONICS, 0.500, true);

        ProductRequestDTO dadosAtualizados
                = new ProductRequestDTO("Celular Teste", ProductCategory.ELECTRONICS, 0.300);

        ProductResponseDTO respostaEsperada
                = new ProductResponseDTO(1L, "Celular Teste novo", ProductCategory.ELECTRONICS, 0.500);

        when(repository.existsByNameIgnoreCase("Celular Teste")).thenReturn(false);
        when(repository.save(any(ProductEntity.class))).thenReturn(produtoOriginal);

        ProductResponseDTO productResponseDTO = service.create(dadosAtualizados);

        assertEquals(productResponseDTO, respostaEsperada);
    }

    @Test
    @DisplayName("Caso em que ja existe um produto com aquele nome")
    void create_context2() {

        ProductRequestDTO dadosAtualizados
                = new ProductRequestDTO("Celular Teste", ProductCategory.ELECTRONICS, 0.300);

        when(repository.existsByNameIgnoreCase("Celular Teste")).thenReturn(true);

        Exception exception
                = assertThrows(FieldAlreadyExistsException.class, () -> service.create(dadosAtualizados));

        assertEquals(ExceptionMessages.PRODUCT_NAME_IN_USE.getMessage("Celular Teste"), exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void update() {
        Long productId = 1L;

        ProductEntity produtoOriginal
                = new ProductEntity(1L, "Celular Teste novo", ProductCategory.ELECTRONICS, 0.500, true);
        ProductRequestDTO dadosAtualizados
                = new ProductRequestDTO("Celular Teste", ProductCategory.ELECTRONICS, 0.300);

        ProductResponseDTO respostaEsperada
                = new ProductResponseDTO(1L, "Celular Teste novo", ProductCategory.ELECTRONICS, 0.500);

        when(repository.findById(productId)).thenReturn(Optional.of(produtoOriginal));
        when(repository.save(any(ProductEntity.class))).thenReturn(produtoOriginal);

        ProductResponseDTO resultadoAtualizado = service.update(productId, dadosAtualizados);


        assertEquals(resultadoAtualizado, respostaEsperada);
    }

    @Test
    @DisplayName("Deve fazer o delete fisico")
    void delete() {

        Long productId = 1L;

        ProductEntity celular
                = new ProductEntity(1L, "Celular", ProductCategory.ELECTRONICS, 0.299, true);

        when(repository.findById(productId)).thenReturn(Optional.of(celular));

        service.delete(productId);

        verify(repository).save(celular);
    }

    @Test
    @DisplayName("Caso em que achamos um produto")
    void findById_context1() {

        Long productId = 1L;

        ProductEntity celular
                = new ProductEntity(1L, "Celular", ProductCategory.ELECTRONICS, 0.299, true);

        when(repository.findById(productId)).thenReturn(Optional.of(celular));

        ProductEntity product = service.findById(productId);

        assertEquals(product, celular);
    }

    @Test
    @DisplayName("Caso em não achamos um produto")
    void findById_context2(){

        Long productId = 1L;

        when(repository.findById(productId)).thenReturn(Optional.empty());

        Exception exception
                = assertThrows(ResourceNotFoundException.class, () -> service.findById(productId));

        assertEquals(ExceptionMessages.PRODUCT_NOT_FOUND_BY_ID.getMessage(productId), exception.getMessage());
    }
}
