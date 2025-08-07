package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import br.com.senior.transport_logistics.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
                .orElseThrow(() -> new RuntimeException("Caminhão não encontrado"));
    }

    private String generateTruckCode(TruckType type) {
        long timeMillisSuffix = System.currentTimeMillis() % 10000;
        String dateTimeCode = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return String.format("TR-%s-%s-%04d", type.name(), dateTimeCode, timeMillisSuffix);
    }
}
