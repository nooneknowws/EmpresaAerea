package br.ufpr.dac.MSReserva.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import br.ufpr.dac.MSReserva.config.rabbitmq.RabbitMQConfig;
import br.ufpr.dac.MSReserva.cqrs.command.ReservaCommandService;
import br.ufpr.dac.MSReserva.events.ReservaEvent;
import br.ufpr.dac.MSReserva.events.ReservaEventMilhas;
import br.ufpr.dac.MSReserva.model.Reserva;
import br.ufpr.dac.MSReserva.model.StatusReserva;
import jakarta.transaction.Transactional;

@Component
public class ReservaMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ReservaMessageListener.class);

    @Autowired
    private ReservaCommandService commandService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ReservaEventMilhas reservaEventMilhas;

   

    @Transactional
    @RabbitListener(queues = "reserva.cancellation.complete")
    public void handleCancellationComplete(Message message, Channel channel) {
        try {
            Map<String, Object> result = objectMapper.readValue(message.getBody(), Map.class);
            Long reservaId = Long.valueOf(result.get("reservaId").toString());
            
            logger.info("Received cancellation completion notification for reserva: {}", reservaId);
            
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error processing cancellation completion", e);
            handleMessageError(channel, message);
        }
    }
    
    @RabbitListener(queues = "reserva.cancelamento.response")
    public void handleCancellationResponse(Map<String, Object> response) {
        Long reservaId = (Long) response.get("reservaId");
        String status = (String) response.get("status");
        
        if ("failure".equals(status)) {
            logger.error("Cancellation failed for reserva: {}", reservaId);
        } else {
            logger.info("Cancellation completed successfully for reserva: {}", reservaId);
        }
    }

    @Transactional
    @RabbitListener(queues = "voo.reserva.atualizacao")
    public void handleVooRealizado(Message message, Channel channel) {
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            
            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            Long vooId = Long.valueOf(payload.get("vooId").toString());
            String acao = (String) payload.get("acao");
            
            List<?> rawReservaIds = (List<?>) payload.get("reservaIds");
            List<Long> reservaIds = rawReservaIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());
            
            logger.info("Recebendo atualização de voo {} com ação {} para {} reservas", 
                vooId, acao, reservaIds.size());
            
            if (reservaIds.isEmpty()) {
                logger.warn("Lista de reservas vazia para o voo {}", vooId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("vooId", vooId);
            response.put("processedReservas", new ArrayList<>());
            response.put("errors", new ArrayList<>());

            for (Long reservaId : reservaIds) {
                try {
                    Optional<Reserva> optReserva = commandService.findById(reservaId);
                    
                    if (optReserva.isPresent()) {
                        Reserva reserva = optReserva.get();
                        StatusReserva novoStatus;

                        if ("ATUALIZAR_STATUS_REALIZADO".equals(acao)) {
                            switch (reserva.getStatus()) {
                                case EMBARCADO:
                                    novoStatus = StatusReserva.REALIZADO;
                                    break;
                                case CANCELADO:
                                    logger.info("Reserva {} ignorada pois está cancelada", reservaId);
                                    continue;
                                default:
                                    novoStatus = StatusReserva.NÃOREALIZADO;
                                    break;
                            }
                        } else if ("ATUALIZAR_STATUS_CANCELADO".equals(acao)) {
                            if (reserva.getStatus() != StatusReserva.CANCELADO) {
                                novoStatus = StatusReserva.CANCELADO;
                                
                                if (reserva.getMilhas() != null && reserva.getMilhas() > 0) {
                                    reservaEventMilhas.publishCancellationRequest(reserva);
                                    logger.info("Iniciado processo de devolução de {} milhas para reserva {}", 
                                        reserva.getMilhas(), reservaId);
                                } else {
                                    logger.info("Reserva {} não utilizou milhas, pulando processo de devolução", 
                                        reservaId);
                                }
                            } else {
                                logger.info("Reserva {} já está cancelada", reservaId);
                                continue;
                            }
                        } else {
                            logger.warn("Ação desconhecida: {}", acao);
                            continue;
                        }

                        Reserva reservaAtualizada = commandService.atualizarStatusReserva(reservaId, novoStatus);

                        ReservaEvent event = ReservaEvent.fromReserva(reservaAtualizada);
                        event.setTipo(ReservaEvent.EventType.UPDATED);
                        rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE_NAME, 
                            RabbitMQConfig.ROUTING_KEY, 
                            event
                        );

                        Map<String, Object> processedReserva = new HashMap<>();
                        processedReserva.put("reservaId", reservaId);
                        processedReserva.put("status", novoStatus);
                        ((List<Map<String, Object>>) response.get("processedReservas")).add(processedReserva);

                        logger.info("Reserva {} atualizada com sucesso para status {}", 
                            reservaId, novoStatus);
                    } else {
                        logger.warn("Reserva {} não encontrada", reservaId);
                        Map<String, Object> error = new HashMap<>();
                        error.put("reservaId", reservaId);
                        error.put("message", "Reserva não encontrada");
                        ((List<Map<String, Object>>) response.get("errors")).add(error);
                    }
                    
                } catch (Exception e) {
                    logger.error("Erro ao processar reserva {}: {}", reservaId, e.getMessage());
                    Map<String, Object> error = new HashMap<>();
                    error.put("reservaId", reservaId);
                    error.put("message", e.getMessage());
                    ((List<Map<String, Object>>) response.get("errors")).add(error);
                }
            }

            rabbitTemplate.convertAndSend(
                "saga-exchange",
                "voo.reserva.response",
                response
            );

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem de atualização de voo: {}", e.getMessage());
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                logger.error("Error rejecting message: {}", ex.getMessage());
            }
        }
    }
    private void handleMessageError(Channel channel, Message message) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ex) {
            logger.error("Error during message rejection", ex);
        }
    }
}