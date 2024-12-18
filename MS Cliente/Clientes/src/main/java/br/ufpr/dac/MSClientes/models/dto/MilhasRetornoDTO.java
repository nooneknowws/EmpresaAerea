package br.ufpr.dac.MSClientes.models.dto;


public class MilhasRetornoDTO {
    private Long clienteId;
    private Double quantidade;
    private String entradaSaida;
    private Double valorEmReais;
    private String descricao;
    private Long reservaId;

    public MilhasRetornoDTO() {
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
}