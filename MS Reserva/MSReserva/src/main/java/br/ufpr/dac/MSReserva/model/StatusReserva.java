package br.ufpr.dac.MSReserva.model;

public enum StatusReserva {
	PENDENTE("Pendente"),
	CONFIRMADO("Confirmado"),
	CANCELADO("Cancelada"), 
	EMBARCADO("Embarcado"),
	REALIZADO("Realizado"),
	NÃOREALIZADO("Não Realizado");
	

	private final String descricao;

	StatusReserva(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}