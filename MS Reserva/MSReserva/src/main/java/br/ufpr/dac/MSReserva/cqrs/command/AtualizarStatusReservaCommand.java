package br.ufpr.dac.MSReserva.cqrs.command;

import br.ufpr.dac.MSReserva.model.StatusReserva;

public record AtualizarStatusReservaCommand(
	    Long reservaId,
	    StatusReserva novoStatus
	) {}