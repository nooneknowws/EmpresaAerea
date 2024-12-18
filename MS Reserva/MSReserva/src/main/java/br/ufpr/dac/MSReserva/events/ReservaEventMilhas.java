package br.ufpr.dac.MSReserva.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufpr.dac.MSReserva.config.rabbitmq.RabbitMQConfig;
import br.ufpr.dac.MSReserva.model.Reserva;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReservaEventMilhas{
    private static final Logger logger = LoggerFactory.getLogger(ReservaEventMilhas.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishSyncEvent(Reserva reserva, ReservaEvent.EventType tipo) {
        logger.info("Publishing {} event for reserva: {}", tipo, reserva.getId());
        
        ReservaEvent event = createEventFromReserva(reserva, tipo);
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            "reserva.sync.event", 
            event
        );
        
        logger.info("Event published successfully");
    }

    public void publishCancellationRequest(Reserva reserva) {
        logger.info("Publishing cancellation request for reserva: {}", reserva.getId());
        
        Map<String, Object> request = new HashMap<>();
        request.put("clienteId", reserva.getClienteId());
        request.put("quantidade", reserva.getMilhas());
        request.put("entradaSaida", "ENTRADA");
        request.put("valorEmReais", reserva.getMilhas() * 5.0);
        request.put("descricao", "RESERVA CANCELADA");
        request.put("reservaId", reserva.getId());
        
        
        rabbitTemplate.convertAndSend(
            "saga-exchange",
            "reserva.cancelamento.request",
            request
        );
        
        logger.info("Cancellation request published successfully");
    }

    private ReservaEvent createEventFromReserva(Reserva reserva, ReservaEvent.EventType tipo) {
        ReservaEvent event = new ReservaEvent();
        event.setTipo(tipo);
        event.setId(reserva.getId());
        event.setDataHora(reserva.getDataHora());
        event.setAeroportoOrigem(reserva.getAeroportoOrigem());
        event.setAeroportoDestino(reserva.getAeroportoDestino());
        event.setValor(reserva.getValor());
        event.setMilhas(reserva.getMilhas());
        event.setStatus(reserva.getStatus().name());
        event.setVooId(reserva.getVooId());
        event.setClienteId(reserva.getClienteId());
        event.setCodigoReserva(reserva.getCodigoReserva());
        event.setCodigoVoo(reserva.getCodigoVoo());
        event.setQuantidade(reserva.getQuantidade());
        return event;
    }
}