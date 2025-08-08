package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;

import java.util.List;
import java.util.Map;

@Service
public class GeminiApiClientService {



    private String construirPromptParaIA(String rotaJson, ShipmentEntity shipment, List<TruckEntity> caminhoesCandidatos) {
        return String.format("""
            Você é um assistente de logística especializado. Sua tarefa é analisar a rota, a carga e uma lista de caminhões pré-qualificados para selecionar o mais eficiente.

            1. Rota Detalhada:
            %s

            2. Especificações da Carga:
            Tipo de Carga: %s
            Peso Total: %.2f toneladas
            Condições Especiais: %s

            3. Modelos de Caminhões Qualificados (já filtrados por capacidade e restrições da rota):
            %s

            Análise e Seleção:
            Com base nos dados acima, selecione o caminhão ideal da lista de qualificados. Priorize a eficiência (menor consumo de combustível) para o percurso. A segurança e conformidade já foram pré-verificadas.

            Formato da Resposta Desejada:
            Forneça apenas um JSON com a seguinte estrutura:
            {
              "caminhaoSugerido": "o id do caminhão escolhido",
              "justificativa": "Texto explicando os motivos da escolha, focando na eficiência e custo.",
              "litrosGastos": "Quantos litros de gasolina ele vai gastar, com base nas informações do modelo"
            }
            """, rotaJson, shipment.getProduct().getCategory(),
                shipment.getWeight(), shipment.getNotes(), caminhoesCandidatos.toString());
    }

    private GeminiResponse parseGeminiResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResponse, GeminiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao parsear resposta da IA", e);
        }
    }
}

