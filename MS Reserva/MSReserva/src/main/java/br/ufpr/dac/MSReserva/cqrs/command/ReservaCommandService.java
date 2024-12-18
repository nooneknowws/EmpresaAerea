package br.ufpr.dac.MSReserva.cqrs.command;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.MSReserva.config.rabbitmq.RabbitMQConfig;
import br.ufpr.dac.MSReserva.dto.CriarReservaDTO;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import br.ufpr.dac.MSReserva.events.ReservaEvent;
import br.ufpr.dac.MSReserva.model.Reserva;
import br.ufpr.dac.MSReserva.model.StatusReserva;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaCommandService {
    @Autowired
    private ReservaCommandRepository reservaCommandRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Transactional
    public Reserva criarReserva(CriarReservaDTO dto) {
        Reserva reserva = new Reserva();
        reserva.setNomeCliente(dto.nomeCliente());
        reserva.setDataHora(LocalDateTime.now());
        reserva.setDataHoraPartida(dto.dataHoraPartida());
        reserva.setAeroportoOrigem(dto.aeroportoOrigem());
        reserva.setAeroportoDestino(dto.aeroportoDestino());
        reserva.setValor(dto.valor());
        reserva.setMilhas(dto.milhas());
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setVooId(dto.vooId());
        reserva.setClienteId(dto.clienteId());
        reserva.setCodigoReserva(gerarCodigoReserva());
        reserva.setCodigoVoo(dto.codigoVoo());      
        reserva.setQuantidade(dto.quantidade());     
        
        Reserva savedReserva = reservaCommandRepository.save(reserva);
        
        ReservaEvent event = ReservaEvent.fromReserva(savedReserva);
        event.setTipo(ReservaEvent.EventType.CREATED); 
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
        
        Map<String, Object> sagaPayload = new HashMap<>();
        sagaPayload.put("reservaId", savedReserva.getId());
        sagaPayload.put("vooId", savedReserva.getVooId());
        sagaPayload.put("quantidade", savedReserva.getQuantidade());
        sagaPayload.put("status", "PENDENTE");

        rabbitTemplate.convertAndSend("saga-exchange", "reserva.criacao.request", sagaPayload);

        return savedReserva;

    }

    @Transactional
    public Reserva atualizarStatusReserva(Long reservaId, StatusReserva novoStatus) {
        Reserva reserva = reservaCommandRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
        
        StatusReserva estadoAnterior = reserva.getStatus();
        reserva.setStatus(novoStatus);
        reserva.adicionarHistoricoAlteracaoEstado(estadoAnterior, novoStatus);
        
        return reservaCommandRepository.save(reserva);
    }

    @Transactional
    public ReservaDTO confirmarReserva(Long id) {
        Reserva reserva = reservaCommandRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            
        if (reserva.getStatus() == StatusReserva.CANCELADO) {
            throw new IllegalStateException("Não é possível confirmar uma reserva cancelada");
        }
        
        StatusReserva estadoOrigem = reserva.getStatus();
        reserva.setStatus(StatusReserva.CONFIRMADO);
        reserva.adicionarHistoricoAlteracaoEstado(estadoOrigem, StatusReserva.CONFIRMADO);
        
        Reserva reservaAtualizada = reservaCommandRepository.save(reserva);
        
        ReservaEvent event = ReservaEvent.fromReserva(reservaAtualizada);
        event.setTipo(ReservaEvent.EventType.UPDATED);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
        
        return reservaAtualizada.toDTO();
    }
    @Transactional
    public ReservaDTO confirmarEmbarque(Long id) {
        Reserva reserva = reservaCommandRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            
        if (reserva.getStatus() != StatusReserva.CONFIRMADO) {
            throw new IllegalStateException("Apenas reservas confirmadas podem ser embarcadas");
        }
        
        StatusReserva estadoOrigem = reserva.getStatus();
        reserva.setStatus(StatusReserva.EMBARCADO);
        reserva.adicionarHistoricoAlteracaoEstado(estadoOrigem, StatusReserva.EMBARCADO);
        
        Reserva reservaAtualizada = reservaCommandRepository.save(reserva);
        
        ReservaEvent event = ReservaEvent.fromReserva(reservaAtualizada);
        event.setTipo(ReservaEvent.EventType.UPDATED);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
        
        return reservaAtualizada.toDTO();
    }
    @Transactional
    public ReservaDTO iniciarCancelamentoReserva(Long id) {
        Reserva reserva = reservaCommandRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            
        if (reserva.getStatus() == StatusReserva.CANCELADO) {
            throw new IllegalStateException("Reserva já está cancelada");
        }
        
        return reserva.toDTO();
    }
    @Transactional
    public ReservaDTO finalizarCancelamentoReserva(Long id) {
        Reserva reserva = reservaCommandRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            
        StatusReserva estadoOrigem = reserva.getStatus();
        reserva.setStatus(StatusReserva.CANCELADO);
        reserva.adicionarHistoricoAlteracaoEstado(estadoOrigem, StatusReserva.CANCELADO);
        
        Reserva reservaAtualizada = reservaCommandRepository.save(reserva);
 
        ReservaEvent event = ReservaEvent.fromReserva(reservaAtualizada);
        event.setTipo(ReservaEvent.EventType.UPDATED);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
        
        return reservaAtualizada.toDTO();
    }

    private String gerarCodigoReserva() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 3).toUpperCase() + "-" + 
               uuid.toString().substring(3, 6).toUpperCase();
    }

    @Transactional(readOnly = true)
    public Optional<Reserva> findById(Long id) {
        return reservaCommandRepository.findById(id);
    }
}