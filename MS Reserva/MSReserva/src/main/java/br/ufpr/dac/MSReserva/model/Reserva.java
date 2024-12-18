package br.ufpr.dac.MSReserva.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.ufpr.dac.MSReserva.dto.HistoricoAlteracaoEstadoDTO;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomeCliente;
    
    @Column(name = "data_hora")
    private LocalDateTime dataHora;
    
    private LocalDateTime dataHoraPartida;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "nome", column = @Column(name = "aeroporto_origem_nome")),
        @AttributeOverride(name = "codigo", column = @Column(name = "aeroporto_origem_codigo")),
        @AttributeOverride(name = "cidade", column = @Column(name = "aeroporto_origem_cidade")),
        @AttributeOverride(name = "estado", column = @Column(name = "aeroporto_origem_estado")),
        @AttributeOverride(name = "pais", column = @Column(name = "aeroporto_origem_pais"))
    })
    private Aeroporto aeroportoOrigem;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "nome", column = @Column(name = "aeroporto_destino_nome")),
        @AttributeOverride(name = "codigo", column = @Column(name = "aeroporto_destino_codigo")),
        @AttributeOverride(name = "cidade", column = @Column(name = "aeroporto_destino_cidade")),
        @AttributeOverride(name = "estado", column = @Column(name = "aeroporto_destino_estado")),
        @AttributeOverride(name = "pais", column = @Column(name = "aeroporto_destino_pais"))
    })
    private Aeroporto aeroportoDestino;
    
    @Column(name = "codigo_voo")
    private String codigoVoo;
    
    @Column(name = "valor")
    private Double valor;
    
    @Column(name = "milhas")
    private Double milhas;
    
    @Column(name = "codigo_reserva")
    private String codigoReserva;
    
    @Column(name = "quantidade")
    private Integer quantidade;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusReserva status;
    
    @Column(name = "voo_id")
    private Long vooId;
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoAlteracaoEstado> historicoAlteracaoEstado = new ArrayList<>();
    
    public ReservaDTO toDTO() {
        List<HistoricoAlteracaoEstadoDTO> historicoAlteracaoEstadoDTO = historicoAlteracaoEstado.stream()
            .map(HistoricoAlteracaoEstado::toDTO)
            .toList();

        return new ReservaDTO(
            id,
            nomeCliente,
            dataHora,
            dataHoraPartida,
            aeroportoOrigem,
            aeroportoDestino,
            valor,
            milhas,
            status.toString(),
            vooId,
            clienteId,
            codigoReserva,
            codigoVoo,
            quantidade,
            historicoAlteracaoEstadoDTO
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Double getMilhas() { return milhas; }
    public void setMilhas(Double milhas) { this.milhas = milhas; }
    
    public String getCodigoReserva() { return codigoReserva; }
	public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }

    public Long getVooId() { return vooId; }
    public void setVooId(Long vooId) { this.vooId = vooId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public List<HistoricoAlteracaoEstado> getHistoricoAlteracaoEstado() { return historicoAlteracaoEstado; }
    public void setHistoricoAlteracaoEstado(List<HistoricoAlteracaoEstado> historicoAlteracaoEstado) { 
        this.historicoAlteracaoEstado = historicoAlteracaoEstado; 
    }

    public void adicionarHistoricoAlteracaoEstado(StatusReserva estadoOrigem, StatusReserva estadoDestino) {
        HistoricoAlteracaoEstado historico = new HistoricoAlteracaoEstado(this, LocalDateTime.now(), estadoOrigem, estadoDestino);
        historicoAlteracaoEstado.add(historico);
    }

	public String getCodigoVoo() {
		return codigoVoo;
	}

	public void setCodigoVoo(String codigoVoo) {
		this.codigoVoo = codigoVoo;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public Aeroporto getAeroportoOrigem() {
		return aeroportoOrigem;
	}

	public void setAeroportoOrigem(Aeroporto aeroportoOrigem) {
		this.aeroportoOrigem = aeroportoOrigem;
	}

	public Aeroporto getAeroportoDestino() {
		return aeroportoDestino;
	}

	public void setAeroportoDestino(Aeroporto aeroportoDestino) {
		this.aeroportoDestino = aeroportoDestino;
	}

	public LocalDateTime getDataHoraPartida() {
		return dataHoraPartida;
	}

	public void setDataHoraPartida(LocalDateTime dataHoraPartida) {
		this.dataHoraPartida = dataHoraPartida;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}
}
