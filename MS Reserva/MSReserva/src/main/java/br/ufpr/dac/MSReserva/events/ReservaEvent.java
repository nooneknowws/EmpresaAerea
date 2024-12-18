package br.ufpr.dac.MSReserva.events;

import java.io.Serializable;
import java.time.LocalDateTime;

import br.ufpr.dac.MSReserva.model.Aeroporto;
import br.ufpr.dac.MSReserva.model.Reserva;
import lombok.Data;

@Data
public class ReservaEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private EventType tipo;
    private Long id;
    private String nomeCliente;
    private LocalDateTime dataHora;
    private LocalDateTime dataHoraPartida;
    private Aeroporto aeroportoOrigem;
    private Aeroporto aeroportoDestino;
    private Double valor;
    private Double milhas;
    private String status;
    private Long vooId;
    private Long clienteId;
    private String codigoVoo;
    private Integer quantidade;
    private String codigoReserva;

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    public static ReservaEvent fromReserva(Reserva reserva) {
        ReservaEvent event = new ReservaEvent();
        event.setNomeCliente(reserva.getNomeCliente());
        event.setId(reserva.getId());
        event.setDataHora(reserva.getDataHora());
        event.setDataHoraPartida(reserva.getDataHoraPartida());
        event.setAeroportoOrigem(reserva.getAeroportoOrigem());
        event.setAeroportoDestino(reserva.getAeroportoDestino());
        event.setValor(reserva.getValor());
        event.setMilhas(reserva.getMilhas());
        event.setStatus(reserva.getStatus().name());
        event.setVooId(reserva.getVooId());
        event.setClienteId(reserva.getClienteId());
        event.setCodigoReserva(reserva.getCodigoReserva());
        event.setCodigoVoo(reserva.getCodigoVoo());
        event.setQuantidade(reserva.getQuantidade()); 

        System.out.println("=== Created Event ===");
        System.out.println(event.toString());
        return event;
    }

	public EventType getTipo() {
		return tipo;
	}

	public void setTipo(EventType tipo) {
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
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

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Double getMilhas() {
		return milhas;
	}

	public void setMilhas(Double milhas) {
		this.milhas = milhas;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getVooId() {
		return vooId;
	}

	public void setVooId(Long vooId) {
		this.vooId = vooId;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
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

	public String getCodigoReserva() {
		return codigoReserva;
	}

	public void setCodigoReserva(String codigoReserva) {
		this.codigoReserva = codigoReserva;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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
