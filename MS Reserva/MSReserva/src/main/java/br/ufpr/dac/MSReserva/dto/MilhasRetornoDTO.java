package br.ufpr.dac.MSReserva.dto;

public class MilhasRetornoDTO {
    private Long clienteId;
    private Double quantidade;
    private String entradaSaida;
    private Double valorEmReais;
    private String descricao;
    private Long reservaId;

    public MilhasRetornoDTO(Long clienteId, Double quantidade, Long reservaId) {
        this.clienteId = clienteId;
        this.quantidade = quantidade;
        this.entradaSaida = "ENTRADA";
        this.valorEmReais = quantidade * 5.0;
        this.descricao = "RESERVA CANCELADA";
        this.reservaId = reservaId;
    }

	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
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

	public Double getValorEmReais() {
		return valorEmReais;
	}

	public void setValorEmReais(Double valorEmReais) {
		this.valorEmReais = valorEmReais;
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

    // Getters and setters
}
