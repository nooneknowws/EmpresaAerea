package br.ufpr.dac.SAGA.orquestrador;

import java.io.IOException;
import java.util.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import br.ufpr.dac.SAGA.models.dto.LoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SagaOrquestrador {

    private static final Logger logger = LoggerFactory.getLogger(SagaOrquestrador.class);

    @Autowired
    private AmqpTemplate rabbitTemplate;

    private Map<String, VerificationStatus> verificationStatusMap = new HashMap<>();

    @RabbitListener(queues = "auth.request", ackMode = "MANUAL")
    public void handleAuthSaga(LoginDTO loginDTO, Message message, Channel channel) {
        try {
            String email = loginDTO.getEmail();
            logger.info("Received login request for email: {}", email);

            verificationStatusMap.put(email, new VerificationStatus());

            rabbitTemplate.convertAndSend("saga-exchange", "client.verification", loginDTO);
            rabbitTemplate.convertAndSend("saga-exchange", "employee.verification", loginDTO);

            logger.info("Sent verification requests for email: {} to client and employee verification queues", email);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error handling auth saga", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                logger.error("Error during message rejection", ioException);
            }
        }
    }

    @RabbitListener(queues = "client.verification.response", ackMode = "MANUAL")
    public void handleClientVerificationResponse(Map<String, String> response, Message message, Channel channel) {
        processResponse(response, "client", channel, message);
    }

    @RabbitListener(queues = "employee.verification.response", ackMode = "MANUAL")
    public void handleEmployeeVerificationResponse(Map<String, String> response, Message message, Channel channel) {
        processResponse(response, "employee", channel, message);
    }

    private void processResponse(Map<String, String> response, String type, Channel channel, Message message) {
        try {
            String email = response.get("email");
            logger.info("Received {} verification response for email: {}", type, email);

            VerificationStatus status = verificationStatusMap.get(email);
            if (status == null) {
                status = new VerificationStatus();
                verificationStatusMap.put(email, status);
            }

            if ("success".equals(response.get("status"))) {
                rabbitTemplate.convertAndSend("saga-exchange", "auth.response", response);
                logger.info("Authentication successful for email: {}", email);
                verificationStatusMap.remove(email); 
            } else {
                logger.info("Email not found in {} service: {}", type, email);

                if ("client".equals(type)) {
                    status.clientVerified = false;
                } else if ("employee".equals(type)) {
                    status.employeeVerified = false;
                }
                if (!status.clientVerified && !status.employeeVerified) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("email", email);
                    errorResponse.put("status", "failure");
                    errorResponse.put("message", "Email not found in both client and employee services.");
                    rabbitTemplate.convertAndSend("saga-exchange", "auth.response", errorResponse);
                    logger.info("Sent failure response for email: {}", email);
                    verificationStatusMap.remove(email); 
                }
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error handling {} verification response", type, e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                logger.error("Error during message rejection", ioException);
            }
        }
    }
    private static class VerificationStatus {
        boolean clientVerified = true;  
        boolean employeeVerified = true; 
    }
}

