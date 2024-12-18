package br.ufpr.dac.SAGA.orquestrador;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;

import br.ufpr.dac.SAGA.models.dto.MilhasRetornoDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReservaOrquestrator {
    private static final Logger logger = LoggerFactory.getLogger(ReservaOrquestrator.class);

    @Autowired
    private AmqpTemplate rabbitTemplate;

    private Map<Long, ProcessamentoStatus> processamentoStatusMap = new HashMap<>();

    @RabbitListener(queues = "reserva.cancelamento.request", ackMode = "MANUAL")
    public void handleReservaCancelamento(Map<String, Object> payload, Message message, Channel channel) {
        try {
            logger.info("Received cancellation request: {}", payload);

            Long reservaId;
            Object rawReservaId = payload.get("reservaId");
            if (rawReservaId instanceof Integer) {
                reservaId = ((Integer) rawReservaId).longValue();
            } else if (rawReservaId instanceof Long) {
                reservaId = (Long) rawReservaId;
            } else {
                reservaId = Long.valueOf(rawReservaId.toString());
            }

            processamentoStatusMap.put(reservaId, new ProcessamentoStatus());

            MilhasRetornoDTO milhasDTO = new MilhasRetornoDTO();
            milhasDTO.setReservaId(reservaId);
            milhasDTO.setQuantidade((Double) payload.get("quantidade"));
            milhasDTO.setClienteId(((Number) payload.get("clienteId")).longValue());
            milhasDTO.setEntradaSaida((String)payload.get("entradaSaida"));
            milhasDTO.setValorEmReais((Double)payload.get("valorEmReais"));
            milhasDTO.setDescricao((String)payload.get("descricao"));

            rabbitTemplate.convertAndSend("saga-exchange", "milhas.processamento", milhasDTO);

            logger.info("Sent milhas return request for reserva: {} with {} milhas", reservaId, milhasDTO.getQuantidade());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error handling reserva cancellation", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                logger.error("Error during message rejection", ioException);
            }
        }
    }


    @RabbitListener(queues = "milhas.processamento.response", ackMode = "MANUAL")
    public void handleMilhasProcessamentoResponse(Map<String, Object> response, Message message, Channel channel) {
        try {
            Long reservaId = convertToLong(response.get("reservaId"));
            logger.info("Received milhas processing response for reserva: {}", reservaId);

            ProcessamentoStatus status = processamentoStatusMap.get(reservaId);
            if (status == null) {
                logger.warn("No status found for reserva: {}", reservaId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            Map<String, Object> responseMessage = new HashMap<>();
            responseMessage.put("reservaId", reservaId);
            
            if ("success".equals(response.get("status"))) {
                responseMessage.put("status", "success");
                responseMessage.put("message", "Milhas successfully processed");
                logger.info("Milhas successfully processed for reserva: {}", reservaId);
            } else {
                responseMessage.put("status", "failure");
                responseMessage.put("message", "Failed to process milhas return");
                logger.error("Failed to process milhas for reserva: {}", reservaId);
            }

            rabbitTemplate.convertAndSend("saga-exchange", "reserva.cancelamento.response", responseMessage);
            processamentoStatusMap.remove(reservaId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            logger.error("Error handling milhas processing response", e);
            handleMessageRejection(message, channel);
        }
    }
    @RabbitListener(queues = "reserva.criacao.request", ackMode = "MANUAL")
    public void handleReservaCriacao(Map<String, Object> payload, Message message, Channel channel) {
        try {
            logger.info("Received creation request: {}", payload);

            Long reservaId = convertToLong(payload.get("reservaId"));
            processamentoStatusMap.put(reservaId, new ProcessamentoStatus());

            Map<String, Object> vooPayload = new HashMap<>();
            vooPayload.put("reservaId", reservaId);
            vooPayload.put("vooId", convertToLong(payload.get("vooId")));
            vooPayload.put("quantidade", payload.get("quantidade"));
            vooPayload.put("status", "PENDENTE");

            rabbitTemplate.convertAndSend("saga-exchange", "voo.atualizacao", vooPayload);

            logger.info("Sent voo update request for reserva: {}", reservaId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error handling reserva creation", e);
            handleMessageRejection(message, channel);
        }
    }

    @RabbitListener(queues = "voo.atualizacao.response", ackMode = "MANUAL")
    public void handleVooAtualizacaoResponse(Map<String, Object> response, Message message, Channel channel) {
        try {
            Long reservaId = convertToLong(response.get("reservaId"));
            logger.info("Received voo update response for reserva: {}", reservaId);

            ProcessamentoStatus status = processamentoStatusMap.get(reservaId);
            if (status == null) {
                logger.warn("No status found for reserva: {}", reservaId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            Map<String, Object> responseMessage = new HashMap<>();
            responseMessage.put("reservaId", reservaId);

            if ("success".equals(response.get("status"))) {
                responseMessage.put("status", "success");
                responseMessage.put("message", "Reserva successfully created");
                logger.info("Voo successfully updated for reserva: {}", reservaId);
            } else {
                responseMessage.put("status", "failure");
                responseMessage.put("message", "Failed to update voo");
                logger.error("Failed to update voo for reserva: {}", reservaId);
            }

            rabbitTemplate.convertAndSend("saga-exchange", "reserva.criacao.response", responseMessage);
            processamentoStatusMap.remove(reservaId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            logger.error("Error handling voo update response", e);
            handleMessageRejection(message, channel);
        }
    }
    
    
    private Long convertToLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("reservaId cannot be null");
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new IllegalArgumentException("reservaId must be either Integer or Long");
    }
    private void handleMessageRejection(Message message, Channel channel) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioException) {
            logger.error("Error during message rejection", ioException);
        }
    }

    private static class ProcessamentoStatus {
        boolean milhasProcessadas = false;
        boolean vooAtualizado = false;
    }
    
}