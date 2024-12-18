package br.ufpr.dac.MSReserva.events;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.MSReserva.cqrs.command.ReservaCommandService;
import br.ufpr.dac.MSReserva.cqrs.query.ReservaQueryRepository;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import br.ufpr.dac.MSReserva.model.Reserva;
import br.ufpr.dac.MSReserva.model.StatusReserva;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Component
public class ReservaEventListener {
    
	 private static final Logger logger = LoggerFactory.getLogger(ReservaEventListener.class);
    @Autowired
    private ReservaQueryRepository reservaQueryRepository;

    @Autowired
    private ReservaCommandService reservaCommandService;

    @Transactional
    @RabbitListener(queues = "reserva.sync.queue")
    public void handleReservaEvent(ReservaEvent event) {
        switch (event.getTipo()) {
            case CREATED:
                Reserva novaReserva = new Reserva();
                populateReservaFromEvent(novaReserva, event);
                novaReserva.adicionarHistoricoAlteracaoEstado(null, StatusReserva.valueOf(event.getStatus()));
                reservaQueryRepository.save(novaReserva);
                break;
                
            case UPDATED:
                Reserva reservaExistente = reservaQueryRepository.findById(event.getId())
                    .orElseGet(() -> new Reserva());
                
                StatusReserva statusAnterior = reservaExistente.getStatus();
                
                populateReservaFromEvent(reservaExistente, event);
                
                StatusReserva novoStatus = StatusReserva.valueOf(event.getStatus());
                if (statusAnterior != novoStatus) {
                    reservaExistente.adicionarHistoricoAlteracaoEstado(statusAnterior, novoStatus);
                }
                
                reservaQueryRepository.save(reservaExistente);
                break;
                
            case DELETED:
                reservaQueryRepository.deleteById(event.getId());
                break;
        }
    }
    
    @RabbitListener(queues = "reserva.cancelamento.response", ackMode = "MANUAL")
    public void handleCancelamentoResponse(Map<String, Object> response, Message message, Channel channel) {
        try {
            Object rawReservaId = response.get("reservaId");
            if (rawReservaId == null) {
                logger.error("Received message without reservaId");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            Long reservaId = convertToLong(rawReservaId);
            String status = Optional.ofNullable(response.get("status"))
                                  .map(Object::toString)
                                  .orElse("unknown");

            logger.info("Received cancellation response for reserva {}: {}", reservaId, status);

            if ("success".equals(status)) {
                ReservaDTO reservaCancelada = reservaCommandService.finalizarCancelamentoReserva(reservaId);
                logger.info("Successfully cancelled reserva: {}", reservaId);
            } else {
                String errorMessage = Optional.ofNullable(response.get("message"))
                                           .map(Object::toString)
                                           .orElse("Unknown error");
                logger.error("Failed to cancel reserva {}: {}", reservaId, errorMessage);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            logger.error("Error processing cancellation response: {}", e.getMessage(), e);
            handleMessageRejection(message, channel);
        }
    }

    private Long convertToLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert value to Long: " + value, e);
        }
    }

    private void handleMessageRejection(Message message, Channel channel) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioException) {
            logger.error("Error during message rejection: {}", ioException.getMessage(), ioException);
        }
    }
    
    private void populateReservaFromEvent(Reserva reserva, ReservaEvent event) {
        reserva.setId(event.getId());
        reserva.setNomeCliente(event.getNomeCliente());
        reserva.setDataHora(event.getDataHora());
        reserva.setDataHoraPartida(event.getDataHoraPartida());
        reserva.setAeroportoOrigem(event.getAeroportoOrigem());
        reserva.setAeroportoDestino(event.getAeroportoDestino());
        reserva.setValor(event.getValor());
        reserva.setMilhas(event.getMilhas());
        reserva.setStatus(StatusReserva.valueOf(event.getStatus()));
        reserva.setVooId(event.getVooId());
        reserva.setClienteId(event.getClienteId());
        reserva.setCodigoReserva(event.getCodigoReserva());
        reserva.setQuantidade(event.getQuantidade());
        reserva.setCodigoVoo(event.getCodigoVoo());
    }
}