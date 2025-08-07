package br.com.senior.transport_logistics.service;

import br.com.senior.transport_logistics.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.OrsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class OpenRouteApiClientService {

    private static final String URL_API_ROTAS = "https://api.openrouteservice.org/v2/directions/foot-walking";
    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api.key}")
    private String chaveApi;

    public double obterDistancia(CoordinatesDTO coordenadaUsuario, CoordinatesDTO coordenadaPonto) {
        String urlPersonalizada = UriComponentsBuilder.fromHttpUrl(URL_API_ROTAS)
                .queryParam("start", coordenadaUsuario.longitude() + "," + coordenadaUsuario.latitude())
                .queryParam("end", coordenadaPonto.longitude() + "," + coordenadaPonto.latitude())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.chaveApi);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<OrsResponse> response = restTemplate.exchange(
                    urlPersonalizada,
                    HttpMethod.GET,
                    entity,
                    OrsResponse.class
            );

            OrsResponse orsResponse = response.getBody();

            if (orsResponse == null || orsResponse.features() == null || orsResponse.features().length == 0) {
                throw new RuntimeException("Nenhuma rota encontrada entre os pontos informados.");
            }

            double distanciaEmMetros = orsResponse.features()[0].properties().segments();

            return distanciaEmMetros / 1000.0;
        } catch (HttpClientErrorException e) {

            throw new RuntimeException("Problema com a requisição para a API de rotas: ");
        }
    }
}
