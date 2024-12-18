package br.ufpr.dac.MSClientes.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.MSClientes.models.dto.LoginDTO;
import br.ufpr.dac.MSClientes.repository.ClienteRepo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ClienteService {
	 private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
	    
	    @Autowired
	    private RabbitTemplate rabbitTemplate;
	    
	    @Autowired
	    private ClienteRepo clienteRepository;
	    
	    private final ConcurrentHashMap<String, CompletableFuture<Map<String, Boolean>>> pendingVerifications = 
	        new ConcurrentHashMap<>();

	    public CompletableFuture<Map<String, Boolean>> verifyRegistrationAvailability(String email, String cpf) {
	        CompletableFuture<Map<String, Boolean>> future = new CompletableFuture<>();
	        String correlationId = UUID.randomUUID().toString();
	        pendingVerifications.put(email, future);
	        
	        try {
	            Map<String, Object> request = new HashMap<>();
	            request.put("email", email);
	            request.put("cpf", cpf);
	            
	            rabbitTemplate.convertAndSend(
	                    "saga-exchange", 
	                    "registration.request",
	                    request,
	                    message -> {
	                        message.getMessageProperties().setCorrelationId(correlationId);
	                        message.getMessageProperties().setReplyTo("client.registration.response");
	                        return message;
	                    }
	                );
	                logger.info("[{}] Registration request sent: email={}, cpf={}", correlationId, email, cpf);
	            
	            setTimeout(() -> {
	                CompletableFuture<Map<String, Boolean>> pending = pendingVerifications.remove(email);
	                if (pending != null && !pending.isDone()) {
	                    Map<String, Boolean> timeoutResult = new HashMap<>();
	                    timeoutResult.put("emailAvailable", false);
	                    timeoutResult.put("cpfAvailable", false);
	                    pending.complete(timeoutResult);
	                    logger.warn("Verification timeout: {}", email);
	                }
	            }, 25000);
	            
	            return future;
	        } catch (Exception e) {
	            logger.error("[{}] Error: {}", correlationId, e.getMessage(), e);
	            return handleVerificationError(email);
	        }
	    }

	    @RabbitListener(queues = "client.registration.response")
	    public void handleVerificationResponse(Map<String, Object> response) {
	        String email = (String) response.get("email");
	        String correlationId = (String) response.get("correlationId");
	        Map<String, Boolean> availability = new HashMap<>();
	        logger.info("[{}] Received verification response for email: {}", correlationId, email);
	        
	        availability.put("emailAvailable", (boolean) response.get("emailAvailable"));
	        availability.put("cpfAvailable", (boolean) response.get("cpfAvailable"));
	        
	        CompletableFuture<Map<String, Boolean>> future = pendingVerifications.remove(email);
	        if (future != null) {
	            future.complete(availability);
	            logger.info("Verification completed: email={}, availability={}", email, availability);
	        }
	    }

	    private CompletableFuture<Map<String, Boolean>> handleVerificationError(String email) {
	        pendingVerifications.remove(email);
	        Map<String, Boolean> errorResult = new HashMap<>();
	        errorResult.put("emailAvailable", false);
	        errorResult.put("cpfAvailable", false);
	        CompletableFuture<Map<String, Boolean>> errorFuture = new CompletableFuture<>();
	        errorFuture.complete(errorResult);
	        return errorFuture;
	    }

	    private void setTimeout(Runnable runnable, int delay) {
	        new Thread(() -> {
	            try {
	                Thread.sleep(delay);
	                runnable.run();
	            } catch (Exception e) {
	                logger.error("Error in timeout", e);
	            }
	        }).start();
	    }

    public LoginDTO verificarCliente(String email, String senha) {
        logger.debug("Attempting to verify client with email: {}", email);
        
        return clienteRepository.findByEmail(email)
                .map(usuario -> {
                    boolean verified = usuario.verificarSenha(senha);
                    logger.debug("Password verification result for {}: {}", email, verified);
                    if (verified) {
                        return new LoginDTO(usuario.getEmail(), null, usuario.getId(), usuario.getPerfil());
                    }
                    return null;
                })
                .orElse(null);
    }
}