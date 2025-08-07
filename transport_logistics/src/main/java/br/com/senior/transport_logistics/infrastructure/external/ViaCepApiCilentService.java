package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO.AddresDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ViaCepApiCilentService {

    private static final String URL_API_ROTAS = "https://viacep.com.br/ws/{cep}/json/";
    private final RestTemplate restTemplate;

    public AddresDTO getAddress(String cep) {
        String url = UriComponentsBuilder.fromHttpUrl(URL_API_ROTAS)
                .buildAndExpand(cep)
                .toUriString();

        try {
            AddresDTO addres = restTemplate.getForObject(url, AddresDTO.class);
            return addres;

        } catch (Exception e) {
            throw new RuntimeException("Não foi possível consultar o CEP: " + cep, e);
        }
    }
}
