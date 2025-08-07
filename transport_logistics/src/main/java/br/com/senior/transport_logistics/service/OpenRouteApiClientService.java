package br.com.senior.transport_logistics.service;

import br.com.senior.transport_logistics.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.ResponseForGemini;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.request.OpenRouteRequestBody;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.request.OptionsRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.request.ProfileParamsRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.OrsResponse;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.RouteRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.SegmentRecord;
import br.com.senior.transport_logistics.dto.OpenRouteDTO.response.StepRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenRouteApiClientService {

    private static final String URL_API_ROTAS = "https://api.openrouteservice.org/v2/directions/foot-walking";
    private final RestTemplate restTemplate;

    @Value("${openrouteservice.api.key}")
    private String chaveApi;

    public ResponseForGemini obterDistancia(CoordinatesDTO start, CoordinatesDTO finish, RestrictionsRecord restrictions) {

        List<List<CoordinatesDTO>> coordinates = List.of(Collections.singletonList(start), Collections.singletonList(finish));

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

            if (responseBody != null && responseBody.routes() != null && !responseBody.routes().isEmpty()) {
                throw new RuntimeException("Nenhuma rota encontrada entre os pontos informados.");
            }

            return new ResponseForGemini(
                    responseBody.routes().get(0).summary().distance(),
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
