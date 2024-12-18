package br.ufpr.dac.SAGA.orquestrador;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.core.Message;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Component
public class VoosOrquestrator {
    private static final Logger logger = LoggerFactory.getLogger(VoosOrquestrator.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "voo.status.request")
    public void handleVooStatusChange(Message message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(
                new String(message.getBody()),
                new TypeReference<Map<String, Object>>() {}
            );

            String tipo = (String) payload.get("tipo");
            Long vooId = Long.valueOf(payload.get("vooId").toString());
            List<Long> reservaIds = (List<Long>) payload.get("reservaIds");

            if ("VOO_REALIZADO".equals(tipo)) {
                logger.info("Recebida mensagem de voo realizado. VooId: {}, Reservas: {}", 
                    vooId, reservaIds.size());

                Map<String, Object> reservaMessage = new HashMap<>();
                reservaMessage.put("vooId", vooId);
                reservaMessage.put("reservaIds", reservaIds);
                reservaMessage.put("acao", "ATUALIZAR_STATUS_REALIZADO");

                logger.info("Enviando solicitação de atualização para MS Reservas");
                rabbitTemplate.convertAndSend(
                    "saga-exchange",
                    "voo.reserva.atualizacao",
                    reservaMessage
                );
            } else if ("VOO_CANCELADO".equals(tipo)) {
                logger.info("Recebida mensagem de voo cancelado. VooId: {}, Reservas: {}", 
                    vooId, reservaIds.size());

                Map<String, Object> reservaMessage = new HashMap<>();
                reservaMessage.put("vooId", vooId);
                reservaMessage.put("reservaIds", reservaIds);
                reservaMessage.put("acao", "ATUALIZAR_STATUS_CANCELADO");

                logger.info("Enviando solicitação de cancelamento para MS Reservas");
                rabbitTemplate.convertAndSend(
                    "saga-exchange",
                    "voo.reserva.atualizacao",
                    reservaMessage
                );
            }

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem de mudança de status do voo: {}", 
                e.getMessage());
        }
    }

    @RabbitListener(queues = "voo.reserva.response")
    public void handleReservaResponse(Message message) {
        try {
            Map<String, Object> response = objectMapper.readValue(
                new String(message.getBody()),
                new TypeReference<Map<String, Object>>() {}
            );

            Long vooId = Long.valueOf(response.get("vooId").toString());
            List<Map<String, Object>> processedReservas = (List<Map<String, Object>>) response.get("processedReservas");
            List<Map<String, Object>> errors = (List<Map<String, Object>>) response.get("errors");

            logger.info("Received response for voo {}: {} reservas processed, {} errors",
                vooId, processedReservas.size(), errors.size());

            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("vooId", vooId);
            statusUpdate.put("processedReservas", processedReservas);
            statusUpdate.put("errors", errors);
            statusUpdate.put("timestamp", System.currentTimeMillis());

            if (!errors.isEmpty()) {
                logger.warn("Errors occurred while processing reservas for voo {}: {}", vooId, errors);
            }

            rabbitTemplate.convertAndSend(
                "saga-exchange",
                "voo.reserva.atualizacao",
                statusUpdate
            );

            logger.info("Sent status update for voo {} back to voo service", vooId);

        } catch (Exception e) {
            logger.error("Error processing reserva response: {}", e.getMessage(), e);
        }
    }
}