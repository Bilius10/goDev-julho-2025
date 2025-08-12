package br.com.senior.transport_logistics.domain.shipment;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.response.ShipmentResponseDTO;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.SHIPMENT_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository repository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public PageDTO<ShipmentResponseDTO> findAll(Pageable pageable){
        Page<ShipmentEntity> shipments = repository.findAll(pageable);

        Page<ShipmentResponseDTO> shipmentsResponse = shipments.map(ShipmentResponseDTO::detailed);

        return new PageDTO<>(
                shipmentsResponse.getContent(),
                shipments.getNumber(),
                shipments.getSize(),
                shipments.getTotalElements(),
                shipments.getTotalPages()
        );
    }

    @Transactional
    public ShipmentResponseDTO create(ShipmentCreateDTO request) {
        ProductEntity product = productService.findById(request.idProduct());

        ShipmentEntity shipmentEntity = new ShipmentEntity(request, product);

        ShipmentEntity savedShipment = repository.save(shipmentEntity);

        return ShipmentResponseDTO.detailed(savedShipment);
    }

    @Transactional
    public ShipmentResponseDTO update(Long id, ShipmentUpdateDTO request) {
        ShipmentEntity shipmentFound = this.findById(id);
        shipmentFound.updateShipment(request, shipmentFound.getProduct());

        ShipmentEntity savedShipment = repository.save(shipmentFound);

        return ShipmentResponseDTO.detailed(savedShipment);
    }

    @Transactional
    public void delete(Long id){
        if(repository.existsById(id)){
            throw new ResourceNotFoundException(SHIPMENT_NOT_FOUND_BY_ID.getMessage(id));
        }

        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ShipmentEntity findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT_NOT_FOUND_BY_ID.getMessage(id)));
    }
}
