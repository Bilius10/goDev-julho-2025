package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.TRUCK_NOT_FOUND_BY_CODE;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository repository;
    private final HubService hubService;

    @Transactional
    public TruckResponseDTO create(TruckRequestDTO request) {
        var hub = hubService.findById(request.hubId());

        var truck = new TruckEntity(request, generateTruckCode(request.type()), hub);

        return TruckResponseDTO.detailed(repository.save(truck));
    }

    @Transactional(readOnly = true)
    public PageDTO<TruckResponseDTO> findAll(TruckStatus status, Pageable pageable) {
        var page = repository.findAll(status, pageable);

        return new PageDTO<>(
                page.map(TruckResponseDTO::basic).stream().toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public TruckResponseDTO findByCode(String code) {
        return TruckResponseDTO.detailed(this.findEntityByCode(code));
    }

    @Transactional
    public void updateStatus(String code, TruckStatus status) {
        var truck = this.findEntityByCode(code);

        truck.setStatus(status);

        repository.save(truck);
    }

    @Transactional(readOnly = true)
    public TruckEntity findEntityByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(TRUCK_NOT_FOUND_BY_CODE.getMessage(code)));
    }

    private String generateTruckCode(TruckType type) {
        long timeMillisSuffix = System.currentTimeMillis() % 10000;
        String dateTimeCode = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return String.format("TR-%s-%s-%04d", type.name(), dateTimeCode, timeMillisSuffix);
    }

    public TruckEntity findById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Caminhão não existe"));
    }

    public AverageDimensionsTrucks findAverageDimensionsTrucks(){
        return repository.findAverageDimensionsTrucks()
                .orElseThrow(() -> new RuntimeException("Nenhum caminhão cadastrado"));
    }

    public List<TruckEntity> findByLoadCapacityGreaterThan(Double loadCapacity, Long idHUb, LocalDate exitDay, LocalDate expectArrivalDay){
        System.out.println(loadCapacity);
        List<TruckEntity> trucks
                = repository.findAvailableTrucksByCapacityAndHubNotInRouteBetween(loadCapacity, idHUb);

        if (trucks.isEmpty()) {
            throw new RuntimeException("Nenhum caminhão encontrado que suporte essa carga.");
        }

        return trucks;
    }
}
