package br.ufpr.dac.MSClientes.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.ufpr.dac.MSClientes.models.dto.MilhasDTO;
import br.ufpr.dac.MSClientes.rest.MilhasRest;
import com.rabbitmq.client.Channel;

import br.ufpr.dac.MSClientes.models.dto.LoginDTO;
import br.ufpr.dac.MSClientes.services.ClienteService;
import br.ufpr.dac.SAGA.dto.MilhasRetornoDTO;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RabbitListenerClientes {

    private static final Logger logger = LoggerFactory.getLogger(RabbitListenerClientes.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MilhasRest milhasRest;

    @RabbitListener(queues = "client.verification", ackMode = "MANUAL")
    public void verifyClient(LoginDTO loginDTO, Message message, Channel channel) {
        try {
            logger.info("Received client verification request for email: {}", loginDTO.getEmail());
           
            LoginDTO clientInfo = clienteService.verificarCliente(loginDTO.getEmail(), loginDTO.getSenha());
            
            if (clientInfo == null) {
                logger.info("Invalid credentials for email: {}", loginDTO.getEmail());
                sendVerificationResponse("failure", "Invalid credentials", null, message, channel);
                return;
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("id", String.valueOf(clientInfo.getId())); 
            response.put("email", clientInfo.getEmail());           
            response.put("perfil", clientInfo.getPerfil());         
            response.put("status", "success");
            response.put("message", "Authentication successful");

            rabbitTemplate.convertAndSend("saga-exchange", "client.verification.response", response);
            logger.info("Sent client verification response to saga-exchange with routing key 'client.verification.response'");

            
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error verifying client", e);
            try {
               
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                logger.error("Error during message rejection", ioException);
            }
        }
    }

    private void sendVerificationResponse(String status, String message, LoginDTO loginDTO, Message messageObj, Channel channel) {
        Map<String, String> response = new HashMap<>();
        if (loginDTO != null) {
            response.put("id", String.valueOf(loginDTO.getId()));
            response.put("email", loginDTO.getEmail());
            response.put("perfil", loginDTO.getPerfil());
        }
        response.put("status", status);
        response.put("message", message);
        
        rabbitTemplate.convertAndSend("saga-exchange", "client.verification.response", response);
        logger.info("Sent client verification response to saga-exchange with routing key 'client.verification.response'");
        try {
            channel.basicAck(messageObj.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            logger.error("Error during message acknowledgment", e);
        }
    }
    @RabbitListener(queues = "milhas.processamento", ackMode = "MANUAL")
    public void processarMilhasRetorno(MilhasRetornoDTO milhasRetornoDTO, Message message, Channel channel) {
        try {
            logger.info("Received milhas return request for clientId: {}", milhasRetornoDTO.getClienteId());

            MilhasDTO milhasDTO = convertToMilhasDTO(milhasRetornoDTO);
            ResponseEntity<?> response = milhasRest.processarMilhas(milhasDTO);

            Map<String, Object> sagaResponse = createSagaResponse(milhasDTO, response);
            
            rabbitTemplate.convertAndSend(
                "saga-exchange",
                "milhas.processamento.response",
                sagaResponse
            );

            logger.info("Processed milhas return for clientId: {} with status: {}",
                milhasDTO.getClienteId(),
                sagaResponse.get("status"));

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            logger.error("Error processing milhas return: {}", e.getMessage(), e);
            handleMessageRejection(message, channel);
        }
    }

    private MilhasDTO convertToMilhasDTO(MilhasRetornoDTO source) {
        MilhasDTO target = new MilhasDTO();
        target.setClienteId(source.getClienteId());
        target.setQuantidade(source.getQuantidade());
        target.setValorEmReais(source.getValorEmReais());
        target.setDescricao(source.getDescricao());
        target.setReservaId(source.getReservaId());
        target.setEntradaSaida(source.getEntradaSaida());
        return target;
    }

    private Map<String, Object> createSagaResponse(MilhasDTO milhasDTO, ResponseEntity<?> response) {
        Map<String, Object> sagaResponse = new HashMap<>();
        sagaResponse.put("reservaId", milhasDTO.getReservaId());
        sagaResponse.put("status", response.getStatusCode().is2xxSuccessful() ? "success" : "failure");
        sagaResponse.put("clienteId", milhasDTO.getClienteId());
        return sagaResponse;
    }

    private void handleMessageRejection(Message message, Channel channel) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioException) {
            logger.error("Error during message rejection", ioException);
        }
    }
   }
