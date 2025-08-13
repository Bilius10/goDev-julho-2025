package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ORSRoute;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.OpenRouteRequestBody;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.OptionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.ProfileParamsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response.OrsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenRouteApiClientService {

    private static final String URL_API_ROTAS = "https://api.openrouteservice.org/v2/directions/driving-hgv/json";
    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api.key}")
    private String chaveApi;

    public ORSRoute obterDistancia(CoordinatesDTO start, CoordinatesDTO finish, RestrictionsRecord restrictions) {

        List<List<Double>> coordinates = List.of(
                List.of(start.latitude(), start.longitude()),
                List.of(finish.latitude(), finish.longitude()),
                List.of(start.latitude(), start.longitude())
        );

        ProfileParamsRecord profileParams = new ProfileParamsRecord(restrictions);
        OptionsRecord options = new OptionsRecord(profileParams);

        OpenRouteRequestBody requestBody = new OpenRouteRequestBody(
                coordinates,
                "recommended",
                options
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.chaveApi);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OpenRouteRequestBody> entity = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<OrsResponse> response = restTemplate.exchange(
                    URL_API_ROTAS,
                    HttpMethod.POST,
                    entity,
                    OrsResponse.class
            );

            OrsResponse responseBody = response.getBody();

            if (responseBody != null && responseBody.routes() != null && responseBody.routes().isEmpty()) {
                throw new RuntimeException("Nenhuma rota encontrada entre os pontos informados.");
            }

            return new ORSRoute(
                    responseBody.routes().get(0).summary().distance()/1000,
                    responseBody.routes().get(0).summary().duration(),
                    responseBody.routes().stream()
                            .flatMap(route -> route.segments().stream())
                            .flatMap(segment -> segment.steps().stream())
                            .toList()
            );

        } catch (HttpClientErrorException e) {

            throw new RuntimeException("Problema com a requisição para a API de rotas: ");
        }
    }
}
