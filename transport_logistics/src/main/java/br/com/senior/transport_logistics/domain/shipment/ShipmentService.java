package br.com.senior.transport_logistics.domain.shipment;

import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.ProductService;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.response.ShipmentResponseDTO;
import br.com.senior.transport_logistics.dto.PageDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository repository;
    private final ProductService productService;

    public PageDTO<ShipmentResponseDTO> findAll(Pageable pageable){
        Page<ShipmentEntity> shipments = repository.findAll(pageable);

        return new PageDTO<>(
                shipments.map(s -> new ShipmentResponseDTO(s.getId(), s.getWeight(),
                        s.getQuantity(), s.getNotes(), s.getProduct().getName(), s.isHazardous())).toList(),
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

        ShipmentEntity saveShipment = repository.save(shipmentEntity);

        return new ShipmentResponseDTO(
                saveShipment.getId(), saveShipment.getWeight(),
                saveShipment.getQuantity(), saveShipment.getNotes(),
                saveShipment.getProduct().getName(), saveShipment.isHazardous()
        );
    }

    public ShipmentResponseDTO update(Long id, ShipmentUpdateDTO request) {
        ShipmentEntity shipmentFound = this.findById(id);
        shipmentFound.updateShipment(request, shipmentFound.getProduct());

        ShipmentEntity saveShipment = repository.save(shipmentFound);

        return new ShipmentResponseDTO(
                saveShipment.getId(), saveShipment.getWeight(),
                saveShipment.getQuantity(), saveShipment.getNotes(),
                saveShipment.getProduct().getName(), saveShipment.isHazardous()
        );
    }

    public void delete(Long id){
        if(repository.existsById(id)){
            throw new RuntimeException("Nenhuma carga encontrada");
        }

        repository.deleteById(id);
    }

    public ShipmentEntity findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhuma carga encontrada"));
    }
}
