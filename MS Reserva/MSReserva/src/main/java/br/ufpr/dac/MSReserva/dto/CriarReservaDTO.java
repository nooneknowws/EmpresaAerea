package br.ufpr.dac.MSReserva.dto;

import java.time.LocalDateTime;

import br.ufpr.dac.MSReserva.model.Aeroporto;

public record CriarReservaDTO(
	String codigoReserva,
	String nomeCliente,
	LocalDateTime dataHoraPartida,
    Long clienteId,
    Long vooId,
    Aeroporto aeroportoOrigem,
    Aeroporto  aeroportoDestino,
    Double valor,
    Double milhas,
    String codigoVoo,
    Integer quantidade
) {}