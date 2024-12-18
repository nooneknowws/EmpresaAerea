package br.ufpr.dac.MSReserva.controller;

import br.ufpr.dac.MSReserva.cqrs.command.ReservaCommandService;
import br.ufpr.dac.MSReserva.cqrs.query.ReservaQueryService;
import br.ufpr.dac.MSReserva.dto.CriarReservaDTO;
import br.ufpr.dac.MSReserva.dto.ReservaDTO;
import br.ufpr.dac.MSReserva.events.ReservaEventMilhas;
import br.ufpr.dac.MSReserva.model.Reserva;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaCommandService commandService;

    @Autowired
    private ReservaQueryService queryService;
    
    @Autowired
    private ReservaEventMilhas eventPublisher;

    @PostMapping
    public ResponseEntity<Reserva> criarReserva(@RequestBody CriarReservaDTO reservaDTO) {
        Reserva reserva = commandService.criarReserva(reservaDTO);
        return ResponseEntity.ok(reserva);
    }
    @GetMapping("/cliente/{clienteId}/filter-data")
    public ResponseEntity<List<ReservaDTO>> listarReservasProximas48HorasPorCliente(
        @PathVariable("clienteId") Long clienteId
    ) {
        System.out.println("Controller: Received request for client " + clienteId + "'s reservations in next 48 hours");
        List<ReservaDTO> reservas = queryService.listarReservasProximas48HorasPorCliente(clienteId);
        System.out.println("Controller: Returning " + reservas.size() + " reservations");
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ReservaDTO>> listarReservasPorCliente(
        @PathVariable("clienteId") Long clienteId) {
        System.out.println("Controller: Received request for clientId: " + clienteId);
        List<ReservaDTO> reservas = queryService.listarReservasPorCliente(clienteId);
        System.out.println("Controller: Returning " + reservas.size() + " reservas");
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/voo/{vooId}")
    public ResponseEntity<List<ReservaDTO>> listarReservasPorVoo(
        @PathVariable("vooId") Long vooId) {
        List<ReservaDTO> reservas = queryService.listarReservasPorVoo(vooId);
        return ResponseEntity.ok(reservas);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obterReservaPorId(@PathVariable("id") Long id) {
        try {
            System.out.println("Controller: Received request for reservation id: " + id);
            ReservaDTO reservaDTO = queryService.consultarReserva(id)
                    .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            System.out.println("Controller: Returning reservation details");
            return ResponseEntity.ok(reservaDTO);
        } catch (EntityNotFoundException e) {
            System.out.println("Controller: Reservation not found for id: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("codigo/{codigoReserva}")
    public ResponseEntity<ReservaDTO> obterReservaPorCodigo(@PathVariable("codigoReserva") String codigoReserva) {
        try {
            System.out.println("Controller: Received request for reservation code: " + codigoReserva);
            ReservaDTO reservaDTO = queryService.buscarReservaPorCodigo(codigoReserva)
                    .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
            System.out.println("Controller: Returning reservation details");
            return ResponseEntity.ok(reservaDTO);
        } catch (EntityNotFoundException e) {
            System.out.println("Controller: Reservation not found for code: " + codigoReserva);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/embarque")
    public ResponseEntity<ReservaDTO> confirmarEmbarque(@PathVariable("id") Long id) {
        try {
            ReservaDTO reservaDTO = commandService.confirmarEmbarque(id);
            return ResponseEntity.ok(reservaDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaDTO> cancelarReserva(@PathVariable("id") Long id) {
        try {
            ReservaDTO reservaDTO = commandService.iniciarCancelamentoReserva(id);
            
            Reserva reserva = commandService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
                    
            eventPublisher.publishCancellationRequest(reserva);
            
            return ResponseEntity.ok(reservaDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}/checkin")
    public ResponseEntity<ReservaDTO> realizarCheckin(@PathVariable("id") Long id) {
        try {
            ReservaDTO reservaDTO = commandService.confirmarReserva(id);
            
            return ResponseEntity.ok(reservaDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}