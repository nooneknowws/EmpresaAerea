package br.ufpr.dac.MSClientes.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "milhas")
public class Milhas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonBackReference
    private Usuario cliente;

    @Column(name = "data_hora_transacao", nullable = false)
    private LocalDateTime dataHoraTransacao;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "entrada_saida", nullable = false)
    private String entradaSaida;
    
    @Column(name = "valor_em_reais", nullable = false)
    private Double valorEmReais;

    private String descricao;
    
    @Column(name = "reserva_id")
    private Long reservaId;

    public Milhas() {}

    public Milhas(Usuario cliente, LocalDateTime dataHoraTransacao, Double quantidade, String entradaSaida, Double valorEmReais,String descricao, Long reservaID) {
        this.cliente = cliente;
        this.dataHoraTransacao = dataHoraTransacao;
        this.quantidade = quantidade;
        this.entradaSaida = entradaSaida;
        this.descricao = descricao;
        this.reservaId = reservaID;
        this.valorEmReais = valorEmReais;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataHoraTransacao() {
        return dataHoraTransacao;
    }

    public void setDataHoraTransacao(LocalDateTime dataHoraTransacao) {
        this.dataHoraTransacao = dataHoraTransacao;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public String getEntradaSaida() {
        return entradaSaida;
    }

    public void setEntradaSaida(String entradaSaida) {
        this.entradaSaida = entradaSaida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

	public Long getReservaId() {
		return reservaId;
	}

	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}

	public Double getValorEmReais() {
		return valorEmReais;
	}

	public void setValorEmReais(Double valorEmReais) {
		this.valorEmReais = valorEmReais;
	}
}