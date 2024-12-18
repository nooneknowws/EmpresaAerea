package br.ufpr.dac.MSReserva.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.ufpr.dac.MSReserva.model.Aeroporto;

public record ReservaDTO(
    Long id,
    String nomeCliente,
    LocalDateTime dataHora,
    LocalDateTime dataHoraPartida,
    Aeroporto aeroportoOrigem,
    Aeroporto aeroportoDestino,
    Double valor,
    Double milhas,
    String status,
    Long vooId,
    Long clienteId,
    String codigoReserva,
    String codigoVoo,
    Integer quantidade,
    List<HistoricoAlteracaoEstadoDTO> historicoAlteracaoEstado
) {}
