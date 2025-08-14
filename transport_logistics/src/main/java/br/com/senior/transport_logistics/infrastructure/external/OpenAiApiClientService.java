package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.TransportRecommendation;
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
public class OpenAiApiClientService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public TransportRecommendation chooseBestTruck(String rotaJson, double distance, ShipmentEntity shipment,
                                                   List<TruckEntity> caminhoesCandidatos, List<ShipmentEntity> pendingShipments) {

        String promptString = construirPromptParaIA(rotaJson, distance, shipment, caminhoesCandidatos, pendingShipments);
        Prompt prompt = new Prompt(promptString);

        ChatResponse response = chatModel.call(prompt);
        String rawResponse = response.getResult().getOutput().getText();

        String cleanedJson = extrairJsonDaString(rawResponse);

        try {

            return objectMapper.readValue(cleanedJson, TransportRecommendation.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro: O JSON recebido da API é inválido.", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Erro: A API retornou um valor não numérico para ID do caminhão ou litros gastos.", e);
        }
    }

    private String construirPromptParaIA(String rotaJson, double distance, ShipmentEntity shipment,
                                         List<TruckEntity> caminhoesCandidatos, List<ShipmentEntity> pendingShipments) {
        try {

            String caminhoesJson = objectMapper.writeValueAsString(caminhoesCandidatos);
            String shipmentsJson = objectMapper.writeValueAsString(pendingShipments);

            return String.format("""
                    ## PERSONA E MISSAO
                    
                    **Persona:** Voce e um Analista de Logistica Senior, especialista em otimizacao de frotas.
                    **Missao:** Sua tarefa e analisar um conjunto de dados para selecionar o caminhao mais adequado para um transporte especifico. A decisao deve ser estritamente baseada em uma metodologia que prioriza **seguranca, conformidade regulatoria, manobrabilidade e custo-beneficio (eficiencia de combustivel)**.
                    
                    ---
                    
                    ## DADOS DE ENTRADA
                    
                    Voce recebera os seguintes dados para analise:
                    
                    1.  **DADOS DA ROTA (Formato GeoJSON):**
                        ```json
                        %s
                        ```
                        - **Distancia Total da Rota:** %s em KM.
                    
                    2.  **DADOS DA CARGA:**
                        - **Tipo de Carga:** %s
                        - **Peso Total:** %.2f kg
                        - **Condicoes Especiais:** %s
                    
                    3.  **DADOS DOS CAMINHOES DISPONIVEIS (Formato JSON):**
                        ```json
                        %s
                        ```
                    
                    4.  **LISTA DE CARGAS PARA RETORNO (Opcional):**
                        *Esta lista pode ser nula. Se fornecida, deve ser obrigatoriamente usada para um possivel retorno caso exista uma carga compativel com o suporte do caminhao.*
                        ```json
                        %s
                        ```
                    
                    ---
                    
                    ## METODOLOGIA DE DECISAO (ORDEM ESTRITA DE PRIORIDADE)
                    
                    Siga estes passos em sequencia. Um caminhao deve ser descartado assim que falhar em um criterio.
                    
                    **1. FILTRO DE SEGURANCA (Carroceria vs. Tipo de Carga):**
                       - **Regra:** Avalie a adequacao da carroceria (`body`) ao tipo de carga. Cargas frageis, de alto valor ou sensiveis (ex: `ELECTRONICS`, `CHEMICALS`) exigem carrocerias especificas e seguras (ex: `REEFER_VAN`, `DRY_VAN`, `TANK`).
                       - **Acao:** Descarte os caminhoes com carrocerias inadequadas para a carga.
                    
                    **2. ANALISE DE MANOBRABILIDADE (Desempate):**
                       - **Contexto:** Use o `length` do caminhao como criterio de desempate se mais de um veiculo passar nos filtros anteriores.
                       - **Regra:** Analise os tipos de via no GeoJSON (`StepRecord[type]`).
                         - **Rotas com predominancia urbana/curvas (type 0, 1, 11):** Prefira o caminhao com o **menor** `length`.
                         - **Rotas com predominancia rodoviaria (type 7, 12):** Caminhoes maiores sao aceitaveis, mas a eficiencia ainda e chave. Se todos os outros criterios forem identicos, o menor `length` ainda e preferivel para versatilidade.
                       - **Acao:** Ordene os caminhoes restantes com base neste criterio.
                    
                    **3. OTIMIZACAO DE CUSTO (Selecao Final):**
                       - **Contexto:** Dentre os caminhoes que restaram, selecione o mais eficiente.
                       - **Regra:** Escolha o caminhao com o **maior** `averageFuelConsumption` (km/L).
                       - **Calculo de Consumo:**            
                         1. Calcule os litros necessarios: `Litros_Gastos = Distancia_KM / averageFuelConsumption`.
                    
                    **4. OTIMIZACAO DE CARGA DE RETORNO (Bonus):**
                       - **Contexto:** Se um caminhao ja foi selecionado nos passos anteriores, verifique se ha uma carga de retorno compativel (em peso e tipo) que tenha como destino o `hub` de origem do caminhao.
                       - **Acao:** Mencione na justificativa se uma carga de retorno foi identificada, reforcando o valor da escolha. Este passo **nao** deve alterar a selecao feita no passo 3.
                    
                    ---
                    
                    ## FORMATO DA RESPOSTA (JSON)
                    
                    Forneca **apenas um JSON valido**, sem nenhum texto ou formatacao externa. Se nenhum caminhao for adequado, retorne `null` nos campos.
                    
                    ```json
                    {
                      "suggestedTruckId": "ID numerico do caminhao escolhido ou null",
                      "returnShipmentId": "ID numerico da CARGA ou null",
                      "truckJustification": "Texto tecnico e conciso explicando a escolha, seguindo a metodologia. Ex: '1. Seguranca: Aprovado (Carroceria DRY_VAN compativel com ELECTRONICS). 2. Manobrabilidade: Criterio de desempate nao foi necessario. 3. Eficiencia: Selecionado por ter o maior consumo medio (2.8 km/L) entre os finalistas.'",
                      "returnShipmentJustification": "Texto tecnico e conciso explicando a escolha, seguindo a metodologia.",
                      "litersSpentOutbound": "Valor numerico (string, duas casas decimais) de litros gastos, ou null."
                      "litersSpentReturn": "Valor numerico (string, duas casas decimais) de litros gastos, ou null."
                    }
                    ```
                    """,

                    rotaJson,
                    distance,
                    shipment.getProduct().getCategory(),
                    shipment.getWeight(),
                    shipment.getNotes(),
                    caminhoesJson,
                    shipmentsJson
            );

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

