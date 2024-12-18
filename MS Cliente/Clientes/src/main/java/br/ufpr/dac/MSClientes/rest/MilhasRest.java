package br.ufpr.dac.MSClientes.rest;

import br.ufpr.dac.MSClientes.models.Milhas;
import br.ufpr.dac.MSClientes.models.Usuario;
import br.ufpr.dac.MSClientes.models.dto.MilhasDTO;
import br.ufpr.dac.MSClientes.models.dto.MilhasRetornoDTO;
import br.ufpr.dac.MSClientes.rabbitmq.RabbitListenerClientes;
import br.ufpr.dac.MSClientes.repository.ClienteRepo;
import br.ufpr.dac.MSClientes.repository.MilhasRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/milhas")
public class MilhasRest {

    private static final Logger logger = LoggerFactory.getLogger(RabbitListenerClientes.class);
    
	@Autowired
    private MilhasRepository milhasRepository;
	@Autowired
    private ClienteRepo usuarioRepository;

	@PostMapping
	public ResponseEntity<?> processarMilhas(@RequestBody MilhasDTO milhasDTO) {
	    logger.info("Starting milhas processing for clientId: {}, amount: {}, type: {}", 
	        milhasDTO.getClienteId(), milhasDTO.getQuantidade(), milhasDTO.getEntradaSaida());
	    
	    try {
	        Usuario cliente = usuarioRepository.findById(milhasDTO.getClienteId())
	            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
	        logger.info("Found client with current balance: {}", cliente.getSaldoMilhas());

	        if (milhasDTO.getEntradaSaida().equals("SAIDA") && cliente.getSaldoMilhas() < milhasDTO.getQuantidade()) {
	            logger.warn("Insufficient balance for client {}. Required: {}, Available: {}", 
	                milhasDTO.getClienteId(), milhasDTO.getQuantidade(), cliente.getSaldoMilhas());
	            return ResponseEntity.badRequest().body("Saldo insuficiente");
	        }

	        Milhas milhas = new Milhas();
	        milhas.setCliente(cliente);
	        milhas.setQuantidade(milhasDTO.getQuantidade());
	        milhas.setEntradaSaida(milhasDTO.getEntradaSaida());
	        milhas.setValorEmReais(milhasDTO.getValorEmReais());
	        milhas.setDescricao(milhasDTO.getDescricao());
	        milhas.setDataHoraTransacao(LocalDateTime.now());
	        milhas.setReservaId(milhasDTO.getReservaId());

	        double oldBalance = cliente.getSaldoMilhas();
	        if (milhasDTO.getEntradaSaida().equals("ENTRADA")) {
	            cliente.setSaldoMilhas(cliente.getSaldoMilhas() + milhasDTO.getQuantidade());
	            logger.info("Adding {} milhas to client {}. Old balance: {}, New balance: {}", 
	                milhasDTO.getQuantidade(), cliente.getId(), oldBalance, cliente.getSaldoMilhas());
	        } else if (milhasDTO.getEntradaSaida().equals("SAIDA")) {
	            cliente.setSaldoMilhas(cliente.getSaldoMilhas() - milhasDTO.getQuantidade());
	            logger.info("Deducting {} milhas from client {}. Old balance: {}, New balance: {}", 
	                milhasDTO.getQuantidade(), cliente.getId(), oldBalance, cliente.getSaldoMilhas());
	        }

	        usuarioRepository.save(cliente);
	        Milhas savedMilhas = milhasRepository.save(milhas);
	        logger.info("Successfully processed milhas transaction. TransactionId: {}", savedMilhas.getId());
	        return ResponseEntity.ok(savedMilhas);
	    } catch (Exception e) {
	        logger.error("Error processing milhas for clientId: {}", milhasDTO.getClienteId(), e);
	        throw e;
	    }
	}

    @GetMapping("/{clienteId}")
    public ResponseEntity<List<Milhas>> getMilhasByCliente(@PathVariable("clienteId") Long clienteId) {
        return ResponseEntity.ok(milhasRepository.findByClienteId(clienteId));
    }

}