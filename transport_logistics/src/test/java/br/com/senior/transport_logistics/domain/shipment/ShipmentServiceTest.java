package br.com.senior.transport_logistics.domain.shipment;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.response.ShipmentResponseDTO;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.SHIPMENT_NOT_FOUND_BY_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @InjectMocks
    private ShipmentService service;

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ProductService productService;

    @Test
    void findAll() {

        Pageable pageable = PageRequest.of(0, 10);
        ProductEntity product = new ProductEntity(1L, "Produto Teste", ProductCategory.AUTOMOTIVE, 1.2f, true);
        ShipmentEntity shipment = new ShipmentEntity(1L, 10.0, 5, "Notas", false, product, TransportStatus.PENDING, null, null);
        Page<ShipmentEntity> shipmentsPage = new PageImpl<>(Collections.singletonList(shipment), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(shipmentsPage);

        PageDTO<ShipmentResponseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(shipment.getId(), result.data().get(0).id());
        assertEquals(shipment.getProduct().getName(), result.data().get(0).productName());

    }

    @Test
    @DisplayName("Deve criar um novo shipment com sucesso")
    void create_context1() {

        ProductEntity product = new ProductEntity(1L, "Produto Teste", ProductCategory.AUTOMOTIVE, 1.2f, true);
        ShipmentCreateDTO requestDTO = new ShipmentCreateDTO(5, "Notas", false, 1L, null, null);
        ShipmentEntity shipmentEntity = new ShipmentEntity(1L, 6.0, 5, "Notas", false, product, TransportStatus.PENDING, null, null);

        when(productService.findById(1L)).thenReturn(product);
        when(repository.save(any(ShipmentEntity.class))).thenReturn(shipmentEntity);


        ShipmentResponseDTO result = service.create(requestDTO);


        assertNotNull(result);
        assertEquals(shipmentEntity.getId(), result.id());
        assertEquals(shipmentEntity.getQuantity(), result.quantity());
    }

    @Test
    @DisplayName("Deve atualizar um shipment existente com sucesso")
    void update() {

        Long shipmentId = 1L;
        ProductEntity product = new ProductEntity(1L, "Produto Teste", ProductCategory.AUTOMOTIVE, 1.2f, true);
        ShipmentEntity shipmentFound = new ShipmentEntity(shipmentId, 10.0, 5, "Notas Antigas", false, product, TransportStatus.PENDING, null, null);
        ShipmentUpdateDTO requestDTO = new ShipmentUpdateDTO(10, "Notas Novas", true, 1L);

        ShipmentEntity shipmentUpdated = new ShipmentEntity(shipmentId, 10.0, 10, "Notas Novas", true, product, TransportStatus.PENDING, null, null);

        when(repository.findById(shipmentId)).thenReturn(Optional.of(shipmentFound));
        when(repository.save(any(ShipmentEntity.class))).thenReturn(shipmentUpdated);

        ShipmentResponseDTO result = service.update(shipmentId, requestDTO);

        assertNotNull(result);
        assertEquals(shipmentUpdated.getId(), result.id());
        assertEquals(shipmentUpdated.getQuantity(), result.quantity());
        assertEquals(shipmentUpdated.getNotes(), result.notes());
        assertEquals(shipmentUpdated.isHazardous(), result.isHazardous());
    }

    @Test
    @DisplayName("Deve deletar um shipment com sucesso")
    void delete_context1() {

        Long shipmentId = 1L;

        when(repository.existsById(shipmentId)).thenReturn(false);

        service.delete(shipmentId);

        verify(repository, times(1)).deleteById(shipmentId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar um shipment inexistente")
    void delete_context2() {

        Long shipmentId = 99L;

        when(repository.existsById(shipmentId)).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(shipmentId));

        verify(repository, times(1)).existsById(shipmentId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve encontrar um shipment por ID com sucesso")
    void findById_context1() {

        Long shipmentId = 1L;
        ProductEntity product = new ProductEntity(1L, "Produto Teste", ProductCategory.AUTOMOTIVE, 1.2f, true);
        ShipmentEntity shipmentFound = new ShipmentEntity(shipmentId, 10.0, 5, "Notas", false, product, TransportStatus.PENDING, null, null);

        when(repository.findById(shipmentId)).thenReturn(Optional.of(shipmentFound));

        ShipmentEntity result = service.findById(shipmentId);

        assertNotNull(result);
        assertEquals(shipmentFound.getId(), result.getId());

        verify(repository, times(1)).findById(shipmentId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao procurar shipment por ID inexistente")
    void findById_context2d() {

        Long shipmentId = 99L;

        when(repository.findById(shipmentId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.findById(shipmentId));
        assertEquals(SHIPMENT_NOT_FOUND_BY_ID.getMessage(shipmentId), exception.getMessage());

        verify(repository, times(1)).findById(shipmentId);
    }

    @Test
    @DisplayName("Caso em que retorna uma lista de shipments pendentes")
    void findAllByStatus(){
        ProductEntity product = new ProductEntity(1L, "Produto Teste", ProductCategory.AUTOMOTIVE, 1.2f, true);
        ShipmentEntity shipment = new ShipmentEntity(1L, 10.0, 5,
                "Notas", false, product, TransportStatus.PENDING, null, null);

        when(repository.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, null, null)).thenReturn(List.of(shipment));

        List<ShipmentEntity> allByStatus = service.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, null, null);

        assertNotNull(allByStatus);
    }
}