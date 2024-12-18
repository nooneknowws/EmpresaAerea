package br.ufpr.dac.MSReserva.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.MSReserva.cqrs.command.ReservaCommandService;
import br.ufpr.dac.MSReserva.cqrs.query.ReservaQueryService;
import br.ufpr.dac.MSReserva.dto.CriarReservaDTO;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import br.ufpr.dac.MSReserva.model.StatusReserva;

@Service
public class ReservaService {

    @Autowired
    private ReservaCommandService reservaCommandService;

    @Autowired
    private ReservaQueryService reservaQueryService;

    public ReservaDTO criarReserva(CriarReservaDTO criarReservaDTO) {
    	return reservaCommandService.criarReserva(criarReservaDTO).toDTO();
    }

    public ReservaDTO consultarReserva(Long id) {
        return reservaQueryService.consultarReserva(id).get();
    }

    public ReservaDTO atualizarStatusReserva(Long reservaId, StatusReserva novoStatus) {
        return reservaCommandService.atualizarStatusReserva(reservaId, novoStatus).toDTO();
    }

    public ReservaDTO cancelarReserva(Long reservaId) {
        return reservaCommandService.atualizarStatusReserva(reservaId, StatusReserva.CANCELADO).toDTO();
    }

    public ReservaDTO realizarCheckIn(Long reservaId) {
        return reservaCommandService.atualizarStatusReserva(reservaId, StatusReserva.EMBARCADO).toDTO();
    }

    public List<ReservaDTO> listarReservasPorCliente(Long clienteId) {
        return reservaQueryService.listarReservasPorCliente(clienteId);
    }

    public List<ReservaDTO> listarReservasPorVoo(Long vooId) {
        return reservaQueryService.listarReservasPorVoo(vooId);
    }
}