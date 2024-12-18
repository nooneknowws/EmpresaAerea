package br.ufpr.dac.MSReserva.dto;

import java.time.LocalDateTime;

public record HistoricoAlteracaoEstadoDTO(
    Long id,
    LocalDateTime dataHoraAlteracao,
    String estadoOrigem,
    String estadoDestino
) {}