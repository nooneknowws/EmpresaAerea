package br.ufpr.dac.MSReserva.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import br.ufpr.dac.MSReserva.dto.HistoricoAlteracaoEstadoDTO;

@Entity
public class HistoricoAlteracaoEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    private LocalDateTime dataHoraAlteracao;

    @Enumerated(EnumType.STRING)
    private StatusReserva estadoOrigem;

    @Enumerated(EnumType.STRING)
    private StatusReserva estadoDestino;

    public HistoricoAlteracaoEstado() {}

    public HistoricoAlteracaoEstado(Reserva reserva, LocalDateTime dataHoraAlteracao, 
                                   StatusReserva estadoOrigem, StatusReserva estadoDestino) {
        this.reserva = reserva;
        this.dataHoraAlteracao = dataHoraAlteracao;
        this.estadoOrigem = estadoOrigem;
        this.estadoDestino = estadoDestino;
    }
    
    public HistoricoAlteracaoEstadoDTO toDTO() {
        return new HistoricoAlteracaoEstadoDTO(
            id,
            dataHoraAlteracao,
            estadoOrigem != null ? estadoOrigem.name() : null,
            estadoDestino != null ? estadoDestino.name() : null
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public LocalDateTime getDataHoraAlteracao() { return dataHoraAlteracao; }
    public void setDataHoraAlteracao(LocalDateTime dataHoraAlteracao) { this.dataHoraAlteracao = dataHoraAlteracao; }

    public StatusReserva getEstadoOrigem() { return estadoOrigem; }
    public void setEstadoOrigem(StatusReserva estadoOrigem) { this.estadoOrigem = estadoOrigem; }

    public StatusReserva getEstadoDestino() { return estadoDestino; }
    public void setEstadoDestino(StatusReserva estadoDestino) { this.estadoDestino = estadoDestino; }
}
