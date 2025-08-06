package br.com.senior.transport_logistics.domain.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubService {

    private HubRepository repository;

    public HubEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhuma filial encontrada"));
    }
}
