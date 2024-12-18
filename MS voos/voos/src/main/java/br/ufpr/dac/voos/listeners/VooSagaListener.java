package br.ufpr.dac.voos.listeners;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import br.ufpr.dac.voos.services.VooService;
import br.ufpr.dac.voos.models.ReservaTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VooSagaListener {
    private static final Logger logger = LoggerFactory.getLogger(VooSagaListener.class);
    
    @Autowired
    private AmqpTemplate rabbitTemplate;
    
    @Autowired
    private VooService vooService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "voo.atualizacao", ackMode = "MANUAL")
    public void handleReservaCriacao(Message message, Channel channel) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            
            Long vooId = convertToLong(payload.get("vooId"));
            Long reservaId = convertToLong(payload.get("reservaId"));
            Integer quantidade = (Integer) payload.get("quantidade");
            String status = (String) payload.get("status");
            
            logger.info("Received reservation creation request for voo {} reserva {}", vooId, reservaId);
            
            ReservaTracking tracking = new ReservaTracking(reservaId, quantidade, status);
            boolean success = vooService.adicionarReservaTracking(vooId, tracking);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reservaId", reservaId);
            response.put("vooId", vooId);
            
            if (success) {
                response.put("status", "success");
                response.put("message", "Successfully added reservation tracking");
                logger.info("Successfully added tracking for reserva {} to voo {}", reservaId, vooId);
            } else {
                response.put("status", "failure");
                response.put("message", "Failed to add reservation tracking");
                logger.error("Failed to add tracking for reserva {} to voo {}", reservaId, vooId);
            }
            
            rabbitTemplate.convertAndSend("saga-exchange", "voo.atualizacao.response", response);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            logger.error("Error processing reservation creation", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                logger.error("Error during message rejection", ex);
            }
        }
    }

    @RabbitListener(queues = "voo.reserva.atualizacao", ackMode = "MANUAL")
    public void handleVooStatusUpdate(Message message, Channel channel) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            Long vooId = convertToLong(payload.get("vooId"));
            List<Map<String, Object>> processedReservas = (List<Map<String, Object>>) payload.get("processedReservas");
            
            logger.info("Received status update for voo {} with {} reservas", vooId, processedReservas.size());
            
            List<ReservaTracking> trackingUpdates = new ArrayList<>();
            for (Map<String, Object> reserva : processedReservas) {
                Long reservaId = convertToLong(reserva.get("reservaId"));
                String status = reserva.get("status").toString();
                
                trackingUpdates.add(new ReservaTracking(
                    reservaId,
                    1,
                    status
                ));
            }
            
            boolean success = vooService.atualizarReservasTracking(vooId, trackingUpdates);
            
            if (success) {
                logger.info("Successfully updated tracking for voo {} with {} reservas", vooId, trackingUpdates.size());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                logger.error("Failed to update tracking for voo {}", vooId);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
            
        } catch (Exception e) {
            logger.error("Error processing voo status update", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                logger.error("Error during message rejection", ex);
            }
        }
    }

    private Long convertToLong(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new IllegalArgumentException("Value must be either Integer or Long");
    }
}