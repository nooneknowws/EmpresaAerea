package br.ufpr.dac.MSReserva.cqrs.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import br.ufpr.dac.MSReserva.model.HistoricoAlteracaoEstado;
import br.ufpr.dac.MSReserva.model.Reserva;

@Service
public class ReservaQueryService {
    @Autowired
    private ReservaQueryRepository reservaQueryRepository;

    private static final Function<Reserva, ReservaDTO> toDTO = reserva -> new ReservaDTO(
        reserva.getId(),
        reserva.getNomeCliente(),
        reserva.getDataHora(),
        reserva.getDataHoraPartida(),
        reserva.getAeroportoOrigem(), 
        reserva.getAeroportoDestino(),
        reserva.getValor(),
        reserva.getMilhas(),
        reserva.getStatus().getDescricao(),
        reserva.getVooId(),
        reserva.getClienteId(),
        reserva.getCodigoReserva(),
        reserva.getCodigoVoo(),
        reserva.getQuantidade(),
        reserva.getHistoricoAlteracaoEstado().stream()
            .map(HistoricoAlteracaoEstado::toDTO)
            .toList()
    );

    private List<ReservaDTO> convertToDTOList(List<Reserva> reservas) {
        return reservas.stream()
            .map(toDTO)
            .toList();
    }

    public List<ReservaDTO> listarTodasReservas() {
        return convertToDTOList(reservaQueryRepository.findAll());
    }

    public Optional<ReservaDTO> consultarReserva(Long id) {
        return reservaQueryRepository.findById(id).map(toDTO);
    }

    public List<ReservaDTO> listarReservasPorCliente(Long clienteId) {
        return convertToDTOList(reservaQueryRepository.findByClienteId(clienteId));
    }

    public List<ReservaDTO> listarReservasPorVoo(Long vooId) {
        return convertToDTOList(reservaQueryRepository.findByVooId(vooId));
    }

    public Optional<ReservaDTO> buscarReservaPorCodigo(String codReserva) {
        return reservaQueryRepository.findByCodigoReserva(codReserva).map(toDTO);
    }
    public List<ReservaDTO> listarReservasProximas48HorasPorCliente(Long clienteId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(48);
        return convertToDTOList(
            reservaQueryRepository.findReservasProximas48HorasPorCliente(now, end, clienteId)
        );
    }
}