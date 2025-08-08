package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.GeminiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiApiClientService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public GeminiResponse chooseBestTruck(String rotaJson, double distance,
                                                ShipmentEntity shipment, List<TruckEntity> caminhoesCandidatos) {

        String promptString = construirPromptParaIA(rotaJson, distance, shipment, caminhoesCandidatos);
        Prompt prompt = new Prompt(promptString);

        ChatResponse response = chatModel.call(prompt);
        String rawResponse = response.getResult().getOutput().getText();


        String cleanedJson = extrairJsonDaString(rawResponse);

        try {

            return objectMapper.readValue(cleanedJson, GeminiResponse.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro: O JSON recebido da API é inválido.", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Erro: A API retornou um valor não numérico para ID do caminhão ou litros gastos.", e);
        }
    }

    private String construirPromptParaIA(String rotaJson, double distance, ShipmentEntity shipment, List<TruckEntity> caminhoesCandidatos) {
        try {

            String caminhoesJson = objectMapper.writeValueAsString(caminhoesCandidatos);

            return String.format("""
                Voce e um Analista de Logistica Senior. Sua missao e analisar os dados para selecionar o caminhao mais eficiente, otimizando seguranca, conformidade e custo.
    
                1. DADOS_ROTA (em formato GeoJSON):
                %s
                
                Distancia total: %s
    
                2. DADOS_CARGA:
                - Tipo de Carga: %s
                - Peso Total: %.2f kg
                - Condicoes Especiais: %s
    
                3. DADOS_CAMINHOES (em formato JSON):
                %s
    
                4. METODOLOGIA (ORDEM ESTRITA DE PRIORIDADE):
                1-SEGURANCA_CARGA (MAXIMA PRIORIDADE): Avalie a adequacao da carroceria ('body') ao tipo de carga. Cargas frageis/valiosas (ex: ELECTRONICS) exigem carroceria fechada (REEFER_VAN, DRY_VAN). Descarte imediatamente caminhoes inadequados.
                2-MANOBRABILIDADE_ROTA: Analise os tipos de via (StepRecord[type]). Rotas urbanas/curvas (type 0,1,11) favorecem caminhoes menores (menor 'length'). Rotas rodoviarias (type 7,12) podem usar caminhoes maiores. Use como criterio de desempate.
                3-EFICIENCIA_COMBUSTIVEL (OTIMIZACAO FINAL): Apos garantir os criterios 1 e 2, selecione o caminhao com maior 'averageFuelConsumption' (em km/L). Calcule a distancia total (km). Calcule os litros gastos: Distancia Total / averageFuelConsumption leve em considereção que a distancia esta em metros.
    
                5. FORMATO_RESPOSTA:
                Forneca apenas um JSON valido, sem nenhum texto ou formatacao externa.
                ```json
                {
                  "caminhaoSugerido": "ID numerico do caminhao escolhido",
                  "justificativa": "Texto tecnico explicando a escolha, seguindo a metodologia (1.Seguranca, 2.Rota, 3.Eficiencia).",
                  "litrosGastos": "Valor numerico (string, duas casas decimais) de litros gastos."
                }
                ```
                """, rotaJson, distance, shipment.getProduct().getCategory(),
                    shipment.getWeight(), shipment.getNotes(), caminhoesJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar a lista de caminhões para JSON.", e);
        }
    }

    private String extrairJsonDaString(String textoCompleto) {
        int inicio = textoCompleto.indexOf('{');
        int fim = textoCompleto.lastIndexOf('}');
        if (inicio != -1 && fim != -1 && fim > inicio) {
            return textoCompleto.substring(inicio, fim + 1);
        }
        throw new IllegalArgumentException("Não foi possível extrair um JSON válido da resposta da API: " + textoCompleto);
    }
}

